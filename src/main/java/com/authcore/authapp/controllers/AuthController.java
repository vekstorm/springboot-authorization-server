package com.authcore.authapp.controllers;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.user.AppUserCreateDto;
import com.authcore.authapp.services.user.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;

    @PostMapping("/create")
    public ResponseEntity<AppResponseDto> createUser(@RequestBody AppUserCreateDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(appUserService.createUser(userDto));
    }

}
