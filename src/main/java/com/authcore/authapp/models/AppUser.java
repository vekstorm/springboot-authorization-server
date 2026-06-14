package com.authcore.authapp.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Entity(name = "app_user")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username", unique = true, length = 50, nullable = false)
    private String username;

    @Column(name = "password", unique = true, length = 100, nullable = false)
    private String password;

    @Column(name = "email", unique = true, length = 100, nullable = false)
    private String email;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "surname1", length = 50)
    private String surname1;

    @Column(name = "surname2", length = 50)
    private String surname2;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "isGoogle")
    private boolean isGoogle = false;

    @Column(name = "isMicrosoft")
    private boolean isMicrosoft = false;

    @Column(name = "isFacebook")
    private boolean isFacebook = false;

    @Column(name = "isGitHub")
    private boolean isGitHub = false;

    private String metadata;

    @Column(name = "expired")
    private boolean expired = false;

    @Column(name = "locked")
    private boolean locked = false;

    @Column(name = "credentialsExpired")
    private boolean credentialsExpired;

    @Column(name = "disabled")
    private boolean disabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "app_user_role", joinColumns = @JoinColumn(name = "app_user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !expired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return !disabled;
    }

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
