package com.authcore.authapp.services.user;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.user.AppUserCreateDto;

public interface AppUserService {


    AppResponseDto createUser (AppUserCreateDto dto);
}
