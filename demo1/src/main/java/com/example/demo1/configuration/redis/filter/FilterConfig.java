package com.example.demo1.configuration.redis.filter;


import com.example.demo1.filter.OAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<OAuthFilter> registrationBean() {
        FilterRegistrationBean<OAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OAuthFilter());
        registration.addUrlPatterns("/v1/plan/*"); // Specify which paths to apply the filter to
        return registration;
    }
}
