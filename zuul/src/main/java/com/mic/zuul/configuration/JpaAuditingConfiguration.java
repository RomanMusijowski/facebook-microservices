package com.mic.zuul.configuration;

import com.mic.zuul.client.AuthClient;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@AllArgsConstructor
public class JpaAuditingConfiguration {

    private final AuthClient authClient;

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(authClient.getCurrentUserInfo().getUsername());
    }
}
