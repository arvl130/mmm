package com.ageulin.mmm.config;

import com.ageulin.mmm.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class SecurityUser implements UserDetails, CredentialsContainer {
    @Getter
    private UUID id;
    private String username;
    private String password;
    private List<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(role -> {
            return new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return "ROLE_" + role.getName().toUpperCase();
                }
            };
        }).toList();
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
