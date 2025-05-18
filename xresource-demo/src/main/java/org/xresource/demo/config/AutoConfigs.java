package org.xresource.demo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xresource.core.auth.XAccessEvaluatorDelegate;
import org.xresource.demo.auth.ResourceAuthProvider;

@Configuration
public class AutoConfigs {

    @Bean
    @ConditionalOnMissingBean(XAccessEvaluatorDelegate.class)
    public XAccessEvaluatorDelegate xAccessEvaluatorDelegate(ResourceAuthProvider provider) {
        return new XAccessEvaluatorDelegate(provider);
    }
}
