package com.ageulin.mmm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionConfig {

    @Bean
    public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> sessionRepositoryCustomizer() {
        return sessionRepository -> sessionRepository
            // The name "sessions" would be preferred here, but unfortunately the
            // name we specify is expanded to [name]_attributes under the hood,
            // so I've opted not to pluralize it.
            .setTableName("session");
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        var serializer = new DefaultCookieSerializer();
        var SEVEN_DAYS = 7 * 24 * 60 * 60;

        serializer.setCookieMaxAge(SEVEN_DAYS);
        return serializer;
    }
}
