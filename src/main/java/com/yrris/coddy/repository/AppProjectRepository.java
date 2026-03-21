package com.yrris.coddy.repository;

import com.yrris.coddy.model.entity.AppProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AppProjectRepository extends JpaRepository<AppProject, Long>, JpaSpecificationExecutor<AppProject> {

    Optional<AppProject> findByIdAndIsDeletedFalse(Long id);

    boolean existsByDeployKeyAndIsDeletedFalse(String deployKey);
}
