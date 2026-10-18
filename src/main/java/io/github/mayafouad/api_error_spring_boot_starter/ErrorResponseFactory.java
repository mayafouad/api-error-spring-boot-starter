package io.github.mayafouad.api_error_spring_boot_starter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ErrorResponseFactory {

    public ApiErrorResponse create(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return create(
                status,
                message,
                request,
                List.of(),
                Map.of()
        );
    }

    public ApiErrorResponse create(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<String> details,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                request.getMethod(),
                List.copyOf(details),
                Map.copyOf(fieldErrors)
        );
    }
}
