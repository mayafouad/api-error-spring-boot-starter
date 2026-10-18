
package io.github.mayafouad.api_error_spring_boot_starter;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String method,
        List<String> details,
        Map<String, String> fieldErrors
) {
}
