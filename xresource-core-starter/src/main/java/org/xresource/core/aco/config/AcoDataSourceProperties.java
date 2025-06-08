package org.xresource.core.aco.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.xresource.internal.exception.XInvalidConfigurationException;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "xresource.aco.datasource")
@ConditionalOnProperty(name = { "xresource.aco.enabled" }, havingValue = "true")
@Getter
@Setter
public class AcoDataSourceProperties {

    @NotBlank(message = "ACO datasource URL must not be blank")
    private String url;

    @NotBlank(message = "ACO datasource username must not be blank")
    private String username;

    @NotBlank(message = "ACO datasource password must not be blank")
    private String password;

    private String driverClassName = "org.postgresql.Driver";

    public void validate() {
        if (url == null || url.isBlank()) {
            throw new XInvalidConfigurationException("ACO datasource URL is required.");
        }
        if (username == null || username.isBlank()) {
            throw new XInvalidConfigurationException("ACO datasource username is required.");
        }
        if (password == null || password.isBlank()) {
            throw new XInvalidConfigurationException("ACO datasource password is required.");
        }
    }
}
