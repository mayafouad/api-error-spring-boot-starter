package io.github.mayafouad.api_error_spring_boot_starter.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.mayafouad.api_error_spring_boot_starter.ApiErrorResponse;
import io.github.mayafouad.api_error_spring_boot_starter.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StandardAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ErrorResponseFactory factory;
    private final ObjectMapper objectMapper;

    public StandardAccessDeniedHandler(
            ErrorResponseFactory factory,
            ObjectMapper objectMapper) {
        this.factory = factory;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException {

        HttpStatus status = HttpStatus.FORBIDDEN;

        ApiErrorResponse body = factory.create(
                status,
                "You do not have permission to access this resource",
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
