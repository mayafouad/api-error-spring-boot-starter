package io.github.mayafouad.api_error_spring_boot_starter.autoconfigure;


import io.github.mayafouad.api_error_spring_boot_starter.ErrorResponseFactory;
import io.github.mayafouad.api_error_spring_boot_starter.security.StandardAccessDeniedHandler;
import io.github.mayafouad.api_error_spring_boot_starter.security.StandardAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@AutoConfiguration(
    after = ErrorStandardizerAutoConfiguration.class
)
@ConditionalOnClass({
    ObjectMapper.class,
    AuthenticationEntryPoint.class,
    AccessDeniedHandler.class
})
public class ErrorStandardizerSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StandardAuthenticationEntryPoint
            standardAuthenticationEntryPoint(
                    ErrorResponseFactory factory,
                    ObjectMapper objectMapper) {

        return new StandardAuthenticationEntryPoint(
                factory, objectMapper
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public StandardAccessDeniedHandler
            standardAccessDeniedHandler(
                    ErrorResponseFactory factory,
                    ObjectMapper objectMapper) {

        return new StandardAccessDeniedHandler(
                factory, objectMapper
        );
    }
}