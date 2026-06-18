package com.authcore.authapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.authcore.authapp.models")
public class AuthApp {

	public static void main(String[] args) {
		SpringApplication.run(AuthApp.class, args);
	}

}
