package com.yrris.coddy.repository;

import com.yrris.coddy.model.entity.AppLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppLikeRepository extends JpaRepository<AppLike, Long> {

    boolean existsByUserIdAndAppId(Long userId, Long appId);

    Optional<AppLike> findByUserIdAndAppId(Long userId, Long appId);

    List<AppLike> findAllByUserIdAndAppIdIn(Long userId, Collection<Long> appIds);
}
