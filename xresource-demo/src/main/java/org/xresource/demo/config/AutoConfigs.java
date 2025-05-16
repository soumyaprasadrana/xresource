package org.xresource.demo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xresource.core.auth.XRoleBasedAccessEvaluator;
import org.xresource.demo.auth.ResourceAuthProvider;

@Configuration
public class AutoConfigs {

    @Bean
    @ConditionalOnMissingBean(XRoleBasedAccessEvaluator.class)
    public XRoleBasedAccessEvaluator xRoleBasedAccessEvaluator(ResourceAuthProvider provider) {
        return new XRoleBasedAccessEvaluator(provider);
    }
}
