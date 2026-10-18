package io.github.mayafouad.api_error_spring_boot_starter;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ErrorHandlerIntegrationTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
    GlobalExceptionHandler.class,
    ErrorResponseFactory.class,
    ErrorHandlerIntegrationTest.TestController.class
})
class ErrorHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @RestController
    static class TestController {

        @GetMapping("/test/missing-parameter")
        String missingParameter(
                @RequestParam("name") String name) {
            return name;
        }

        @GetMapping("/test/type-mismatch")
        String typeMismatch(
                @RequestParam("id") Long id) {
            return id.toString();
        }

        @GetMapping("/test/not-found")
        void notFound() {
            throw new EntityNotFoundException("Test entity not found");
        }

        @GetMapping("/test/response-status")
        void responseStatus() {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Test conflict"
            );
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("Internal test detail");
        }
    }

    @Test
    void missingRequestParameterReturns400() throws Exception {
        assertStandardError(
            mockMvc.perform(get("/test/missing-parameter")),
            400,
            "/test/missing-parameter"
        );
    }

    @Test
    void invalidParameterTypeReturns400() throws Exception {
        assertStandardError(
            mockMvc.perform(
                get("/test/type-mismatch").param("id", "abc")
            ),
            400,
            "/test/type-mismatch"
        );
    }

    @Test
    void entityNotFoundReturns404() throws Exception {
        assertStandardError(
            mockMvc.perform(get("/test/not-found")),
            404,
            "/test/not-found"
        );
    }

    @Test
    void responseStatusExceptionPreservesStatus() throws Exception {
        assertStandardError(
            mockMvc.perform(get("/test/response-status")),
            409,
            "/test/response-status"
        );
    }

    @Test
    void unexpectedExceptionReturns500WithoutLeakingDetails()
            throws Exception {

        String json = mockMvc.perform(get("/test/unexpected"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode body = objectMapper.readTree(json);

        assertCommonFields(body, 500, "/test/unexpected");

        org.junit.jupiter.api.Assertions.assertFalse(
            json.contains("Internal test detail"),
            "Internal exception details must not be exposed"
        );
    }

    private void assertStandardError(
            org.springframework.test.web.servlet.ResultActions action,
            int expectedStatus,
            String expectedPath) throws Exception {

        String json = action
            .andExpect(status().is(expectedStatus))
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            ))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode body = objectMapper.readTree(json);
        assertCommonFields(body, expectedStatus, expectedPath);
    }

    private void assertCommonFields(
            JsonNode body,
            int expectedStatus,
            String expectedPath) {

        org.junit.jupiter.api.Assertions.assertTrue(
            body.hasNonNull("timestamp")
        );
        org.junit.jupiter.api.Assertions.assertEquals(
            expectedStatus, body.get("status").asInt()
        );
        org.junit.jupiter.api.Assertions.assertTrue(
            body.hasNonNull("error")
        );
        org.junit.jupiter.api.Assertions.assertTrue(
            body.hasNonNull("message")
        );
        org.junit.jupiter.api.Assertions.assertEquals(
            expectedPath, body.get("path").asText()
        );
    }
}
