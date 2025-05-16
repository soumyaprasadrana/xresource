package org.xresource.core.config;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.xresource.core.auth.XRoleBasedAccessEvaluator;
import org.xresource.core.controller.XResourceController;
import org.xresource.core.openapi.XOpenApiGenerator;
import org.xresource.core.registry.XResourceMetadataRegistry;
import org.xresource.core.service.XResourceService;

@Configuration
@ConditionalOnProperty(prefix = "xresource.openapi", name = "enabled", havingValue = "true")
public class XResourceOpenApiConfig {

    @Value("${xresource.openapi.enabled:true}")
    private boolean openApiEnabled;

    @PostConstruct
    public void validateSpringDocDependency() {
        if (openApiEnabled) {
            try {
                // This will only be present if springdoc-openapi is on the classpath
                Class.forName("org.springdoc.core.properties.SpringDocConfigProperties");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        """
                                xresource.openapi.enabled=true but SpringDoc (springdoc-openapi) is not present on the classpath.
                                Either:
                                - Set xresource.openapi.enabled=false
                                - Or include springdoc-openapi-ui in your dependencies:

                                Maven:
                                <dependency>
                                    <groupId>org.springdoc</groupId>
                                    <artifactId>springdoc-openapi-ui</artifactId>
                                    <version>2.8.8</version>
                                </dependency>

                                Gradle:
                                implementation 'org.springdoc:springdoc-openapi-ui:2.8.8'
                                """);
            }
        }
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(XOpenApiGenerator.class)
    public XOpenApiGenerator xOpenApiGenerator(XResourceMetadataRegistry registry,
            @Value("${xresource.api.base-path:/api/resources}") String basePath,
            XRoleBasedAccessEvaluator xRoleBasedAccessEvaluator, XResourceService xResourceService) {
        return new XOpenApiGenerator(registry, basePath, xRoleBasedAccessEvaluator, xResourceService);
    }

}