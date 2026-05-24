package com.authcore.authapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
public class AppResponseDto {

    @Getter
    private HttpStatus status;

    @Getter
    private String message;

}
