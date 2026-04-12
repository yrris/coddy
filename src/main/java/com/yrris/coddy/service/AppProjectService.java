package com.yrris.coddy.service;

import com.yrris.coddy.model.dto.app.*;
import com.yrris.coddy.model.vo.AppVO;
import com.yrris.coddy.model.vo.LoginUserVO;
import com.yrris.coddy.model.vo.PageVO;
import reactor.core.publisher.Flux;

public interface AppProjectService {

    Long addApp(AppAddRequest request, LoginUserVO loginUser);

    boolean updateApp(AppUpdateRequest request, LoginUserVO loginUser);

    boolean deleteApp(Long appId, LoginUserVO loginUser);

    AppVO getAppVO(Long appId);

    AppVO getAppVOForAdmin(Long appId);

    PageVO<AppVO> listMyApps(AppQueryRequest request, LoginUserVO loginUser);

    PageVO<AppVO> listGoodApps(AppQueryRequest request);

    boolean deleteAppByAdmin(Long appId);

    boolean updateAppByAdmin(AppAdminUpdateRequest request);

    PageVO<AppVO> listAppsByAdmin(AppQueryRequest request);

    Flux<String> chatToGenCode(Long appId, String message, LoginUserVO loginUser);

    String deployApp(Long appId, LoginUserVO loginUser);

    String generateScreenshot(Long appId, LoginUserVO loginUser);

    boolean publishApp(Long appId, LoginUserVO loginUser);

    boolean unpublishApp(Long appId, LoginUserVO loginUser);

    PageVO<AppVO> listPublicApps(AppQueryRequest request, Long currentUserId);

    PageVO<AppVO> listFeaturedApps(AppQueryRequest request, Long currentUserId);

    boolean featureApp(Long appId);

    boolean unfeatureApp(Long appId);

    PageVO<AppVO> listMyLikedApps(AppQueryRequest request, LoginUserVO loginUser);
}
