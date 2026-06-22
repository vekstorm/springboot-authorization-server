package com.authcore.authapp.repository;

import com.authcore.authapp.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    boolean existsByRolesId(UUID roleId);

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    Page<AppUser> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<AppUser> findByEmailContainingIgnoreCase(String email, Pageable pageable);

}
