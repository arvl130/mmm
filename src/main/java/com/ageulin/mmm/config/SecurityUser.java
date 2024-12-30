package com.ageulin.mmm.config;

import com.ageulin.mmm.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@AllArgsConstructor
public class SecurityUser implements UserDetails, CredentialsContainer {
    // Spring Session serializes the fields we have here when creating a session,
    // so make sure that all fields defined here are easily serializable
    // (e.g. don't add a field here that points to a class with Spring Data JPA relations).
    @Getter
    private UUID id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
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
