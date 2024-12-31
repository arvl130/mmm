package com.ageulin.mmm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.time.Duration;

@Configuration
public class SessionConfig {
    private final Duration SESSION_VALID_DURATION = Duration.ofDays(7);

    @Bean
    public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> sessionRepositoryCustomizer() {
        return sessionRepository -> {
            // The name "sessions" would be preferred here, but unfortunately the
            // name we specify is expanded to [name]_attributes under the hood,
            // so I've opted not to pluralize it.
            sessionRepository.setTableName("session");
            // This should be greater than or equal to the cookie max age.
            // Otherwise, we will have cookies saved on client browsers
            // referencing a session that no longer exists.
            //
            // Ideally, cookies and its corresponding session should be
            // invalidated at the same time. Hence, we make them match here.
            sessionRepository.setDefaultMaxInactiveInterval(this.SESSION_VALID_DURATION);
        };
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        var serializer = new DefaultCookieSerializer();

        serializer.setCookieMaxAge(Math.toIntExact(this.SESSION_VALID_DURATION.toSeconds()));
        return serializer;
    }
}
