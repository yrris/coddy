package com.yrris.coddy.repository;

import com.yrris.coddy.model.entity.AppUser;
import com.yrris.coddy.model.enums.AuthProviderEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailAndIsDeletedFalse(String email);

    Optional<AppUser> findByIdAndIsDeletedFalse(Long id);

    boolean existsByEmailAndIsDeletedFalse(String email);

    Optional<AppUser> findByAuthProviderAndProviderUserIdAndIsDeletedFalse(AuthProviderEnum authProvider, String providerUserId);

    Page<AppUser> findAllByIsDeletedFalse(Pageable pageable);
}
