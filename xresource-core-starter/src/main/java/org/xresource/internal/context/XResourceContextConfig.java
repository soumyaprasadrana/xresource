package org.xresource.internal.context;

import static org.xresource.internal.config.XResourceConfigProperties.API_BASE_PATH;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XResourceContextConfig {

    @Bean
    public FilterRegistrationBean<XResourceRequestContextFilter> xResourceRequestContextFilter(
            @Value(API_BASE_PATH) String apiBase) {
        FilterRegistrationBean<XResourceRequestContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XResourceRequestContextFilter(apiBase));
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
