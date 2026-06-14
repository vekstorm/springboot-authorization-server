package com.authcore.authapp.repository;

import com.authcore.authapp.models.Client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByClientId(String clientID);

    Page<Client> findByClientNameContainingIgnoreCase(String clientName, Pageable pageable);

    Page<Client> findAll(Pageable pageable);

}
