package com.yrris.coddy.repository;

import com.yrris.coddy.model.entity.AppProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppProjectRepository extends JpaRepository<AppProject, Long>, JpaSpecificationExecutor<AppProject> {

    Optional<AppProject> findByIdAndIsDeletedFalse(Long id);

    boolean existsByDeployKeyAndIsDeletedFalse(String deployKey);

    @Query("SELECT ap FROM AppProject ap WHERE ap.id IN " +
           "(SELECT al.appId FROM AppLike al WHERE al.userId = :userId) " +
           "AND ap.isPublic = true AND ap.isDeleted = false")
    Page<AppProject> findLikedByUser(@Param("userId") Long userId, Pageable pageable);
}
