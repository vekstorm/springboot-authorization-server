package com.authcore.authapp.repository;

import com.authcore.authapp.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long>{

    Optional<Client> findByClientId(String clientID);

}
