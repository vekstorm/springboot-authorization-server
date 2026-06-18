package com.authcore.authapp.services.user;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.user.AppUserCreateDto;
import com.authcore.authapp.dto.user.AppUserResponseDto;
import com.authcore.authapp.dto.user.AppUserUpdateDto;
import com.authcore.authapp.models.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppUserService {

    AppResponseDto createUser(AppUserCreateDto dto);

    AppUserResponseDto getUserById(String id);

    Page<AppUserResponseDto> getUsers(String search, Pageable pageable);

    AppResponseDto updateUser(String id, AppUserUpdateDto dto);

    AppResponseDto deleteUser(String id);

    AppUser findOrCreateOAuth2User(String email, String givenName, String familyName);

    AppUser findByEmail(String email);
}
