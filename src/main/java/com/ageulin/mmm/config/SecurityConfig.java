package com.ageulin.mmm.config;

import com.ageulin.mmm.dtos.responses.SignOutResponse;
import com.ageulin.mmm.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .csrf(Customizer.withDefaults())
            .logout(configure -> configure
                .logoutUrl("/api/v1/auth/signout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    var signOutResponse = new SignOutResponse("Sign out success.");
                    var objectMapper = new ObjectMapper();
                    var writer = response.getWriter();
                    var json = objectMapper.writeValueAsString(signOutResponse);

                    response.setStatus(HttpStatus.OK.value());
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    writer.print(json);
                    writer.flush();
                })
            )
            .authorizeHttpRequests(authorize -> authorize
                 .requestMatchers(
                     "/api/v1/memes",
                     "/api/v1/memes/*"
                 ).authenticated()
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserRepository userRepository) {
        var provider = new DaoAuthenticationProvider();

        // We can define this as a bean, so that it can be injected in other places,
        // but for now we only use it here, so there's no need to do that.
        provider.setUserDetailsService(username -> {
            var user = userRepository.findByEmail(username);
            if (user.isEmpty()) {
                throw new UsernameNotFoundException("Incorrect username or password.");
            }

            var u = user.get();
            return new SecurityUser(u.getId(), u.getEmail(), u.getPassword(), u.getRoles());
        });

        return new ProviderManager(provider);
    }
}
