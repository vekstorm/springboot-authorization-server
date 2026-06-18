package com.authcore.authapp.dto.user.mapper;

import com.authcore.authapp.dto.user.AppUserResponseDto;
import com.authcore.authapp.models.AppUser;
import com.authcore.authapp.models.Role;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AppUserMapper {

    public Page<AppUserResponseDto> toResponseDtoPage(Page<AppUser> users) {
        return users.map(this::toResponseDto);
    }

    public AppUserResponseDto toResponseDto(AppUser user) {
        AppUserResponseDto dto = new AppUserResponseDto();
        dto.setId(user.getId().toString());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setSurname1(user.getSurname1());
        dto.setSurname2(user.getSurname2());
        dto.setAddress(user.getAddress());
        dto.setPhone(user.getPhone());
        dto.setGoogle(user.isGoogle());
        dto.setMicrosoft(user.isMicrosoft());
        dto.setFacebook(user.isFacebook());
        dto.setGitHub(user.isGitHub());
        dto.setMetadata(user.getMetadata());
        dto.setExpired(user.isExpired());
        dto.setLocked(user.isLocked());
        dto.setCredentialsExpired(user.isCredentialsExpired());
        dto.setDisabled(user.isDisabled());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
