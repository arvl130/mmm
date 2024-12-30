package com.ageulin.mmm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;

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
}
