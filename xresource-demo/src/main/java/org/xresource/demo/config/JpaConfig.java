package org.xresource.demo.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "org.xresource.demo.repository")
@EntityScan(basePackages = "org.xresource.demo.entity")
public class JpaConfig {
    // Configuration class for JPA repositories
}