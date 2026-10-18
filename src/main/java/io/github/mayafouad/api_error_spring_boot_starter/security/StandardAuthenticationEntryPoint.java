package io.github.mayafouad.api_error_spring_boot_starter.security;


import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.mayafouad.api_error_spring_boot_starter.ApiErrorResponse;
import io.github.mayafouad.api_error_spring_boot_starter.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StandardAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ErrorResponseFactory factory;
    private final ObjectMapper objectMapper;

    public StandardAuthenticationEntryPoint(
            ErrorResponseFactory factory,
            ObjectMapper objectMapper) {
        this.factory = factory;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ApiErrorResponse body = factory.create(
                status,
                "Authentication is required",
                request
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}