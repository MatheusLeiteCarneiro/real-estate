package com.mlcdev.realestate;

import com.mlcdev.realestate.dto.PropertyFilter;
import com.mlcdev.realestate.service.ImageService;
import com.mlcdev.realestate.service.PropertyService;
import com.mlcdev.realestate.service.UserService;
import com.mlcdev.realestate.util.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@PostgresIntegrationTest
@AutoConfigureMockMvc
class RealEstateApplicationIT {

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

    @TestConfiguration
    static class AuthorizationServerTestConfig {

        @Bean
        RegisteredClientRepository registeredClientRepository() {
            RegisteredClient client = RegisteredClient.withId(UUID.nameUUIDFromBytes("test-client".getBytes()).toString())
                    .clientId("test-client")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost/callback")
                    .scope(OidcScopes.OPENID)
                    .build();

            return new InMemoryRegisteredClientRepository(client);
        }
    }
}
