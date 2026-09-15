package com.mlcdev.realestate.security;

import com.mlcdev.realestate.dto.ImageDTO;
import com.mlcdev.realestate.dto.PropertyDetailDTO;
import com.mlcdev.realestate.dto.PropertyFilter;
import com.mlcdev.realestate.dto.PropertyPatchDTO;
import com.mlcdev.realestate.exception.NotFoundException;
import com.mlcdev.realestate.service.ImageService;
import com.mlcdev.realestate.service.PropertyService;
import com.mlcdev.realestate.service.UserService;
import com.mlcdev.realestate.util.PostgresIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@PostgresIntegrationTest
@AutoConfigureMockMvc
@Import(PropertyVisibilitySecurityITConfig.class)
class PropertyVisibilitySecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PropertyService propertyService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ImageService imageService;

    @Nested
    @DisplayName("Property details")
    class PropertyDetailsTests {

        @Test
        @DisplayName("Should enforce property visibility by role")
        void findPropertyByIdShouldReturnExpectedStatusAccordingToAvailabilityAndRole() throws Exception {
            UUID availablePropertyId = UUID.randomUUID();
            UUID unavailablePropertyId = UUID.randomUUID();
            PropertyDetailDTO availableProperty = propertyDetail(availablePropertyId, true);
            PropertyDetailDTO unavailableProperty = propertyDetail(unavailablePropertyId, false);

            when(propertyService.findById(availablePropertyId, false)).thenReturn(availableProperty);
            when(propertyService.findById(unavailablePropertyId, false))
                    .thenThrow(new NotFoundException("Property with ID: " + unavailablePropertyId + " not found"));
            when(propertyService.findById(unavailablePropertyId, true)).thenReturn(unavailableProperty);

            mockMvc.perform(get("/v1/properties/{id}", availablePropertyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(availablePropertyId.toString()))
                    .andExpect(jsonPath("$.available").value(true));

            mockMvc.perform(get("/v1/properties/{id}", unavailablePropertyId))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/v1/properties/{id}", unavailablePropertyId)
                            .with(brokerJwt(UUID.randomUUID())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(false));

            mockMvc.perform(get("/v1/properties/{id}", unavailablePropertyId)
                            .with(adminJwt(UUID.randomUUID())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(false));

            verify(propertyService).findById(availablePropertyId, false);
            verify(propertyService).findById(unavailablePropertyId, false);
            verify(propertyService, times(2)).findById(unavailablePropertyId, true);
        }
    }

    @Nested
    @DisplayName("All properties")
    class AllPropertiesTests {

        @Test
        @DisplayName("Should allow only brokers and admins")
        void findAllPropertiesShouldAllowOnlyBrokerAndAdmin() throws Exception {
            when(propertyService.findAll(any(Pageable.class), any(PropertyFilter.class)))
                    .thenReturn(Page.empty());

            mockMvc.perform(get("/v1/properties/all"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/v1/properties/all")
                            .with(brokerJwt(UUID.randomUUID())))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/v1/properties/all")
                            .with(adminJwt(UUID.randomUUID())))
                    .andExpect(status().isOk());

            verify(propertyService, times(2))
                    .findAll(any(Pageable.class), any(PropertyFilter.class));
        }
    }

    @Nested
    @DisplayName("Property images")
    class PropertyImagesTests {

        @Test
        @DisplayName("Should enforce image visibility by role")
        void findAllImagesShouldReturnExpectedStatusAccordingToAvailabilityAndRole() throws Exception {
            UUID availablePropertyId = UUID.randomUUID();
            UUID unavailablePropertyId = UUID.randomUUID();
            ImageDTO image = ImageDTO.builder().id(UUID.randomUUID()).isPrimary(true).build();

            when(imageService.findAllImages(availablePropertyId, false)).thenReturn(List.of(image));
            when(imageService.findAllImages(unavailablePropertyId, false))
                    .thenThrow(new NotFoundException("Property with ID: " + unavailablePropertyId + " not found"));
            when(imageService.findAllImages(unavailablePropertyId, true)).thenReturn(List.of(image));

            mockMvc.perform(get("/v1/properties/{propertyId}/images", availablePropertyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(image.getId().toString()));

            mockMvc.perform(get("/v1/properties/{propertyId}/images", unavailablePropertyId))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/v1/properties/{propertyId}/images", unavailablePropertyId)
                            .with(brokerJwt(UUID.randomUUID())))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/v1/properties/{propertyId}/images", unavailablePropertyId)
                            .with(adminJwt(UUID.randomUUID())))
                    .andExpect(status().isOk());

            verify(imageService).findAllImages(availablePropertyId, false);
            verify(imageService).findAllImages(unavailablePropertyId, false);
            verify(imageService, times(2)).findAllImages(unavailablePropertyId, true);
        }

        @Test
        @DisplayName("Should enforce primary image visibility by role")
        void findPrimaryImageShouldReturnExpectedStatusAccordingToAvailabilityAndRole() throws Exception {
            UUID availablePropertyId = UUID.randomUUID();
            UUID unavailablePropertyId = UUID.randomUUID();
            ImageDTO image = ImageDTO.builder().id(UUID.randomUUID()).isPrimary(true).build();

            when(imageService.findPrimaryImage(availablePropertyId, false)).thenReturn(image);
            when(imageService.findPrimaryImage(unavailablePropertyId, false))
                    .thenThrow(new NotFoundException("Property with ID: " + unavailablePropertyId + " not found"));
            when(imageService.findPrimaryImage(unavailablePropertyId, true)).thenReturn(image);

            mockMvc.perform(get("/v1/properties/{propertyId}/images/primary", availablePropertyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(image.getId().toString()));

            mockMvc.perform(get("/v1/properties/{propertyId}/images/primary", unavailablePropertyId))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/v1/properties/{propertyId}/images/primary", unavailablePropertyId)
                            .with(brokerJwt(UUID.randomUUID())))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/v1/properties/{propertyId}/images/primary", unavailablePropertyId)
                            .with(adminJwt(UUID.randomUUID())))
                    .andExpect(status().isOk());

            verify(imageService).findPrimaryImage(availablePropertyId, false);
            verify(imageService).findPrimaryImage(unavailablePropertyId, false);
            verify(imageService, times(2)).findPrimaryImage(unavailablePropertyId, true);
        }
    }

    @Nested
    @DisplayName("Ownership rules")
    class OwnershipRulesTests {

        @Test
        @DisplayName("Should allow reading but deny modification")
        void nonOwnerBrokerShouldReadUnavailablePropertyButNotModifyIt() throws Exception {
            UUID propertyId = UUID.randomUUID();
            UUID brokerId = UUID.randomUUID();
            PropertyDetailDTO unavailableProperty = propertyDetail(propertyId, false);

            when(propertyService.findById(propertyId, true)).thenReturn(unavailableProperty);
            when(propertyService.update(
                    eq(propertyId),
                    any(PropertyPatchDTO.class),
                    eq(brokerId),
                    eq(false)
            )).thenThrow(new AccessDeniedException("Access Denied"));
            doThrow(new AccessDeniedException("Access Denied"))
                    .when(propertyService)
                    .delete(propertyId, brokerId, false);

            RequestPostProcessor broker = brokerJwt(brokerId);

            mockMvc.perform(get("/v1/properties/{id}", propertyId).with(broker))
                    .andExpect(status().isOk());

            mockMvc.perform(patch("/v1/properties/{id}", propertyId)
                            .with(brokerJwt(brokerId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(delete("/v1/properties/{id}", propertyId)
                            .with(brokerJwt(brokerId)))
                    .andExpect(status().isForbidden());

            verify(propertyService).findById(propertyId, true);
            verify(propertyService).update(
                    eq(propertyId),
                    any(PropertyPatchDTO.class),
                    eq(brokerId),
                    eq(false)
            );
            verify(propertyService).delete(propertyId, brokerId, false);
        }
    }

    private PropertyDetailDTO propertyDetail(UUID propertyId, boolean available) {
        return PropertyDetailDTO.builder()
                .id(propertyId)
                .title("Test property")
                .available(available)
                .build();
    }

    private RequestPostProcessor brokerJwt(UUID brokerId) {
        return jwt()
                .jwt(token -> token
                        .subject(brokerId.toString())
                        .claim("authorities", List.of("ROLE_BROKER")))
                .authorities(new SimpleGrantedAuthority("ROLE_BROKER"));
    }

    private RequestPostProcessor adminJwt(UUID adminId) {
        return jwt()
                .jwt(token -> token
                        .subject(adminId.toString())
                        .claim("authorities", List.of("ROLE_ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

}

@TestConfiguration(proxyBeanMethods = false)
class PropertyVisibilitySecurityITConfig {

    @Bean
    RegisteredClientRepository registeredClientRepository() {
        RegisteredClient client = RegisteredClient
                .withId(UUID.nameUUIDFromBytes("test-client".getBytes()).toString())
                .clientId("test-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/callback")
                .scope(OidcScopes.OPENID)
                .build();

        return new InMemoryRegisteredClientRepository(client);
    }
}
