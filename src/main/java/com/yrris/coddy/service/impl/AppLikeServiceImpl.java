package com.yrris.coddy.service.impl;

import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import com.yrris.coddy.model.entity.AppLike;
import com.yrris.coddy.model.entity.AppProject;
import com.yrris.coddy.model.vo.LoginUserVO;
import com.yrris.coddy.repository.AppLikeRepository;
import com.yrris.coddy.repository.AppProjectRepository;
import com.yrris.coddy.service.AppLikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppLikeServiceImpl implements AppLikeService {

    private final AppLikeRepository appLikeRepository;
    private final AppProjectRepository appProjectRepository;

    public AppLikeServiceImpl(AppLikeRepository appLikeRepository, AppProjectRepository appProjectRepository) {
        this.appLikeRepository = appLikeRepository;
        this.appProjectRepository = appProjectRepository;
    }

    @Override
    @Transactional
    public boolean likeApp(Long appId, LoginUserVO loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }

        AppProject app = appProjectRepository.findByIdAndIsDeletedFalse(appId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "App not found"));

        if (!Boolean.TRUE.equals(app.getIsPublic())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Can only like public apps");
        }

        if (appLikeRepository.existsByUserIdAndAppId(loginUser.getId(), appId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Already liked this app");
        }

        AppLike appLike = new AppLike();
        appLike.setUserId(loginUser.getId());
        appLike.setAppId(appId);
        appLikeRepository.save(appLike);

        app.setLikeCount(app.getLikeCount() + 1);
        appProjectRepository.save(app);

        return true;
    }

    @Override
    @Transactional
    public boolean unlikeApp(Long appId, LoginUserVO loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }

        AppLike appLike = appLikeRepository.findByUserIdAndAppId(loginUser.getId(), appId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Like not found"));

        appLikeRepository.delete(appLike);

        AppProject app = appProjectRepository.findByIdAndIsDeletedFalse(appId).orElse(null);
        if (app != null && app.getLikeCount() > 0) {
            app.setLikeCount(app.getLikeCount() - 1);
            appProjectRepository.save(app);
        }

        return true;
    }

    @Override
    public Set<Long> getLikedAppIds(Long userId, Collection<Long> appIds) {
        if (userId == null || appIds == null || appIds.isEmpty()) {
            return Collections.emptySet();
        }
        return appLikeRepository.findAllByUserIdAndAppIdIn(userId, appIds)
                .stream()
                .map(AppLike::getAppId)
                .collect(Collectors.toSet());
    }
}
