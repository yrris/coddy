package com.yrris.coddy.service.impl;

import com.yrris.coddy.constant.UserSessionConstant;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import com.yrris.coddy.model.dto.user.UserLoginRequest;
import com.yrris.coddy.model.dto.user.UserRegisterRequest;
import com.yrris.coddy.model.entity.AppUser;
import com.yrris.coddy.model.enums.AuthProviderEnum;
import com.yrris.coddy.model.enums.UserRoleEnum;
import com.yrris.coddy.model.vo.LoginUserVO;
import com.yrris.coddy.repository.AppUserRepository;
import com.yrris.coddy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Long register(UserRegisterRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Request is required");
        }
        if (!request.getPassword().equals(request.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Passwords do not match");
        }
        if (appUserRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Email already registered");
        }

        AppUser user = new AppUser();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(resolveDisplayName(request.getDisplayName(), request.getEmail()));
        user.setAuthProvider(AuthProviderEnum.LOCAL);
        user.setUserRole(UserRoleEnum.USER);
        user.setIsDeleted(false);
        appUserRepository.save(user);

        return user.getId();
    }

    @Override
    public LoginUserVO login(UserLoginRequest request, HttpServletRequest httpServletRequest) {
        AppUser user = appUserRepository.findByEmailAndIsDeletedFalse(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Email or password is incorrect"));

        if (!StringUtils.hasText(user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "This account must use Google login");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Email or password is incorrect");
        }

        writeLoginSession(httpServletRequest, user);
        return toLoginUserVO(user);
    }

    @Override
    @Transactional
    public LoginUserVO loginWithGoogle(
            String email,
            String displayName,
            String avatarUrl,
            String providerUserId,
            HttpServletRequest httpServletRequest
    ) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Google email is required");
        }

        String normalizedEmail = email.trim().toLowerCase();

        AppUser user = appUserRepository.findByEmailAndIsDeletedFalse(normalizedEmail).orElseGet(() -> {
            AppUser newUser = new AppUser();
            newUser.setEmail(normalizedEmail);
            newUser.setDisplayName(resolveDisplayName(displayName, normalizedEmail));
            newUser.setAvatarUrl(avatarUrl);
            newUser.setAuthProvider(AuthProviderEnum.GOOGLE);
            newUser.setProviderUserId(providerUserId);
            newUser.setUserRole(UserRoleEnum.USER);
            newUser.setIsDeleted(false);
            return newUser;
        });

        if (!StringUtils.hasText(user.getDisplayName()) && StringUtils.hasText(displayName)) {
            user.setDisplayName(displayName);
        }
        if (!StringUtils.hasText(user.getAvatarUrl()) && StringUtils.hasText(avatarUrl)) {
            user.setAvatarUrl(avatarUrl);
        }
        if (user.getAuthProvider() == AuthProviderEnum.GOOGLE) {
            user.setProviderUserId(providerUserId);
        }

        appUserRepository.save(user);
        writeLoginSession(httpServletRequest, user);
        return toLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUser(HttpServletRequest httpServletRequest) {
        Long loginUserId = getLoginUserId(httpServletRequest);
        AppUser user = appUserRepository.findByIdAndIsDeletedFalse(loginUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Login required"));
        return toLoginUserVO(user);
    }

    @Override
    public boolean logout(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return true;
    }

    @Override
    public boolean isAdmin(HttpServletRequest httpServletRequest) {
        Long userId = getLoginUserId(httpServletRequest);
        AppUser user = appUserRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Login required"));
        return UserRoleEnum.isAdmin(user.getUserRole());
    }

    @Override
    public Page<LoginUserVO> listUsers(long current, long pageSize) {
        long safeCurrent = Math.max(current, 1L);
        long safePageSize = Math.min(Math.max(pageSize, 1L), 50L);

        Page<AppUser> page = appUserRepository.findAllByIsDeletedFalse(
                PageRequest.of((int) (safeCurrent - 1), (int) safePageSize)
        );
        return page.map(this::toLoginUserVO);
    }

    private LoginUserVO toLoginUserVO(AppUser user) {
        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setDisplayName(user.getDisplayName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setAuthProvider(user.getAuthProvider());
        vo.setUserRole(user.getUserRole());
        return vo;
    }

    private String resolveDisplayName(String displayName, String email) {
        if (StringUtils.hasText(displayName)) {
            return displayName.trim();
        }
        int separator = email.indexOf('@');
        if (separator > 0) {
            return email.substring(0, separator);
        }
        return "new-user";
    }

    private void writeLoginSession(HttpServletRequest request, AppUser user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(UserSessionConstant.LOGIN_USER_ID, user.getId());
        session.setAttribute(UserSessionConstant.LOGIN_USER_ROLE, user.getUserRole().name());
    }

    private Long getLoginUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }
        Object rawValue = session.getAttribute(UserSessionConstant.LOGIN_USER_ID);
        if (!(rawValue instanceof Long loginUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }
        return loginUserId;
    }
}
