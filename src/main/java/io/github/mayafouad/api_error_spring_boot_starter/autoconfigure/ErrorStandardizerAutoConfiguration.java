package io.github.mayafouad.api_error_spring_boot_starter.autoconfigure;


import io.github.mayafouad.api_error_spring_boot_starter.ErrorResponseFactory;
import io.github.mayafouad.api_error_spring_boot_starter.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

@AutoConfiguration
public class ErrorStandardizerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ErrorResponseFactory errorResponseFactory() {
        return new ErrorResponseFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(
            ErrorResponseFactory factory) {
        return new GlobalExceptionHandler(factory);
    }
}
