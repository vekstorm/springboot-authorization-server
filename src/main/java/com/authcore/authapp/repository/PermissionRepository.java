package com.authcore.authapp.repository;

import com.authcore.authapp.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByName(String name);

    List<Permission> findByActiveTrue();

    boolean existsByName(String name);

    Page<Permission> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
