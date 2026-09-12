package com.acme.salary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.acme.salary.repository")
public class JpaAuditingConfiguration {
}
