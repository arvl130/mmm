package com.ageulin.mmm.config;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class UsernameAndPasswordUser extends SecurityUser implements UserDetails, CredentialsContainer {
    // Spring Session serializes the fields that we have here when creating a session,
    // so make sure that all fields defined here are easily serializable
    // (e.g. don't add a field here that points to a class with Spring Data JPA relations).
    private final String username;
    private String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsernameAndPasswordUser(
        UUID id,
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(id);

        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

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
