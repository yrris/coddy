package com.yrris.coddy.service;

import com.yrris.coddy.model.vo.LoginUserVO;

import java.util.Collection;
import java.util.Set;

public interface AppLikeService {

    boolean likeApp(Long appId, LoginUserVO loginUser);

    boolean unlikeApp(Long appId, LoginUserVO loginUser);

    Set<Long> getLikedAppIds(Long userId, Collection<Long> appIds);
}
