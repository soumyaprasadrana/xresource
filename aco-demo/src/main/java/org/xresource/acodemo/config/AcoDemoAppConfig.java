package org.xresource.acodemo.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "org.xresource.acodemo.repositories")
@EntityScan(basePackages = "org.xresource.acodemo.entity")
public class AcoDemoAppConfig {

}
