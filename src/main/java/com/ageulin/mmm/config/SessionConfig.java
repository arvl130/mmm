package com.ageulin.mmm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.time.Duration;

@Configuration
public class SessionConfig {
    @Bean
    public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> sessionRepositoryCustomizer() {
        return sessionRepository -> {
            // The name "sessions" would be preferred here, but unfortunately the
            // name we specify is expanded to [name]_attributes under the hood,
            // so I've opted not to pluralize it.
            sessionRepository.setTableName("session");
        };
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        var serializer = new DefaultCookieSerializer();

        // For some reason (framework bug maybe?), Spring Session isn't
        // setting this value on our session cookie so let's just do it manually.
        serializer.setCookieMaxAge(Integer.MAX_VALUE);
        serializer.setRememberMeRequestAttribute(SpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR);
        return serializer;
    }
}
