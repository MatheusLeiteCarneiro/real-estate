package com.mlcdev.realestate.api;

import com.mlcdev.realestate.api.dto.PropertyFilter;
import com.mlcdev.realestate.api.service.ImageService;
import com.mlcdev.realestate.api.service.PropertyService;
import com.mlcdev.realestate.api.service.UserService;
import com.mlcdev.realestate.api.util.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@PostgresIntegrationTest
@AutoConfigureMockMvc
class ApiApplicationIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PropertyService propertyService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ImageService imageService;

    @Test
    void contextLoads() {
    }

    @Test
    void actuatorHealthShouldReturnOk() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void publicEndpointShouldBeAllowed() throws Exception {
        when(propertyService.findAllAvailable(
                ArgumentMatchers.any(Pageable.class),
                ArgumentMatchers.any(PropertyFilter.class))
        ).thenReturn(Page.empty());

        mockMvc.perform(get("/v1/properties"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointShouldBeBlockedWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

}
