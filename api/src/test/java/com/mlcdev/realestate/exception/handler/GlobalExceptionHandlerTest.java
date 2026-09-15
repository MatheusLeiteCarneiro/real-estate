package com.mlcdev.realestate.exception.handler;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.core.TypeInformation;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ReflectionTestUtils.setField(
                handler,
                "maxFileSize",
                "10MB"
        );

        ReflectionTestUtils.setField(
                handler,
                "maxRequestSize",
                "100MB"
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(handler)
                .build();
    }

    @Test
    void shouldReturnBadRequestForInvalidUuid() throws Exception {
        mockMvc.perform(get("/test/uuid/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Invalid value for request parameter"))
                .andExpect(jsonPath("$.path")
                        .value("/test/uuid/not-a-uuid"));
    }

    @Test
    void shouldReturnBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Malformed or invalid request body"))
                .andExpect(jsonPath("$.path")
                        .value("/test/body"));
    }

    @Test
    void shouldReturnBadRequestForInvalidBodyEnum() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test property",
                                  "transactionType": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Malformed or invalid request body"))
                .andExpect(jsonPath("$.path")
                        .value("/test/body"));
    }

    @Test
    void shouldReturnValidationErrorsForInvalidBody() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "transactionType": "SALE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/test/body"))
                .andExpect(jsonPath("$.errors[0].fieldName")
                        .value("name"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Name is required"));
    }

    @Test
    void shouldReturnBadRequestForInvalidSortProperty() throws Exception {
        mockMvc.perform(get("/test/sort"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Invalid sort property"))
                .andExpect(jsonPath("$.path")
                        .value("/test/sort"));
    }

    @Test
    void shouldReturnConflictForDataIntegrityViolation() throws Exception {
        mockMvc.perform(get("/test/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error")
                        .value("Request conflicts with existing data or database constraints"))
                .andExpect(jsonPath("$.path")
                        .value("/test/integrity"));
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException()
            throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path")
                        .value("/test/unexpected"));
    }

    @Test
    void shouldReturnContentTooLargeForOversizedUpload()
            throws Exception {
        mockMvc.perform(get("/test/upload"))
                .andExpect(status().is(413))
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.error")
                        .value(
                                "Uploaded file or request exceeds the maximum "
                                        + "allowed size. Max file size: 10MB. "
                                        + "Max request size: 100MB"
                        ))
                .andExpect(jsonPath("$.path")
                        .value("/test/upload"));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/uuid/{id}")
        UUID receiveUuid(@PathVariable("id") UUID id) {
            return id;
        }

        @PostMapping("/body")
        TestRequest receiveBody(@Valid @RequestBody TestRequest request) {
            return request;
        }

        @GetMapping("/sort")
        void invalidSort() {
            throw new PropertyReferenceException(
                    "doesNotExist",
                    TypeInformation.of(TestRequest.class),
                    List.of()
            );
        }

        @GetMapping("/integrity")
        void dataIntegrity() {
            throw new DataIntegrityViolationException(
                    "Database constraint"
            );
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException(
                    "Unexpected failure"
            );
        }

        @GetMapping("/upload")
        void uploadTooLarge() {
            throw new MaxUploadSizeExceededException(1L);
        }
    }

    record TestRequest(
            @NotBlank(message = "Name is required")
            String name,
            TestTransactionType transactionType
    ) {
    }

    enum TestTransactionType {
        SALE,
        RENT
    }
}
