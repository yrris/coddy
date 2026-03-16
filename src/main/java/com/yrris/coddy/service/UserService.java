package com.yrris.coddy.service;

import com.yrris.coddy.model.dto.user.UserLoginRequest;
import com.yrris.coddy.model.dto.user.UserRegisterRequest;
import com.yrris.coddy.model.vo.LoginUserVO;
import org.springframework.data.domain.Page;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    Long register(UserRegisterRequest request);

    LoginUserVO login(UserLoginRequest request, HttpServletRequest httpServletRequest);

    LoginUserVO loginWithGoogle(
            String email,
            String displayName,
            String avatarUrl,
            String providerUserId,
            HttpServletRequest httpServletRequest
    );

    LoginUserVO getLoginUser(HttpServletRequest httpServletRequest);

    boolean logout(HttpServletRequest httpServletRequest);

    boolean isAdmin(HttpServletRequest httpServletRequest);

    Page<LoginUserVO> listUsers(long current, long pageSize);
}
