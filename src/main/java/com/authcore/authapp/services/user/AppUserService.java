package com.authcore.authapp.services.user;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.user.AppUserCreateDto;
import com.authcore.authapp.models.AppUser;

public interface AppUserService {

    AppResponseDto createUser(AppUserCreateDto dto);

    AppUser findOrCreateOAuth2User(String email, String givenName, String familyName);

    AppUser findByEmail(String email);
}
