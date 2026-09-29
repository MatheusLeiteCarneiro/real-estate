package com.mlcdev.realestate.auth;

import com.jayway.jsonpath.JsonPath;
import com.mlcdev.realestate.auth.config.properties.BffClientProperties;
import com.mlcdev.realestate.auth.util.PostgresIntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@PostgresIntegrationTest
@AutoConfigureMockMvc
class OAuth2AuthorizationServerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private BffClientProperties bffClient;

    @Value("${security.authorization-server.url}")
    private String issuer;

    @Test
    void authorizeRedirectsAnonymousUsersToLogin() throws Exception {
        mockMvc.perform(get(authorizeUri("abc")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void discoveryEndpointExposesIssuer() throws Exception {
        mockMvc.perform(get("/.well-known/openid-configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issuer").value(issuer));
    }

    @Test
    void tokenEndpointRejectsInvalidClientCredentials() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(bffClient.clientId(), "wrong-secret"))
                        .param("grant_type", "authorization_code")
                        .param("code", "12345")
                        .param("redirect_uri", bffClient.redirectUri()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorizationCodeFlowIssuesAccessTokenWithCustomClaims() throws Exception {
        String username = "broker-" + UUID.randomUUID();
        UUID userId = seedUser(username, "Broker123!", "ROLE_BROKER");

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", "Broker123!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");
        assertThat(sessionCookie).isNotNull();

        String codeVerifier = randomCodeVerifier();
        String codeChallenge = codeChallengeS256(codeVerifier);

        MvcResult authorizeResult = mockMvc.perform(get(authorizeUri(codeChallenge)).cookie(sessionCookie))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String location = authorizeResult.getResponse().getHeader(HttpHeaders.LOCATION);
        assertThat(location).isNotNull().startsWith(bffClient.redirectUri());
        String code = UriComponentsBuilder.fromUriString(location).build().getQueryParams().getFirst("code");
        assertThat(code).isNotBlank();

        MvcResult tokenResult = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(bffClient.clientId(), bffClient.clientSecret()))
                        .param("grant_type", "authorization_code")
                        .param("code", code)
                        .param("redirect_uri", bffClient.redirectUri())
                        .param("code_verifier", codeVerifier))
                .andExpect(status().isOk())
                .andReturn();

        String tokenJson = tokenResult.getResponse().getContentAsString();
        Jwt accessToken = jwtDecoder.decode(JsonPath.read(tokenJson, "$.access_token"));
        assertThat(accessToken.getSubject()).isEqualTo(userId.toString());
        assertThat(accessToken.getClaimAsString("username")).isEqualTo(username);
        assertThat(accessToken.getClaimAsStringList("authorities")).contains("ROLE_BROKER");

        Jwt idToken = jwtDecoder.decode(JsonPath.read(tokenJson, "$.id_token"));
        assertThat(idToken.getSubject()).isEqualTo(username);
    }

    private UUID seedUser(String username, String rawPassword, String... authorities) {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "INSERT INTO tb_user (id, username, password, active, created_at, updated_at) VALUES (?, ?, ?, true, ?, ?)",
                id, username, passwordEncoder.encode(rawPassword), now, now
        );
        for (String authority : authorities) {
            jdbcTemplate.update("INSERT INTO tb_user_role (user_id, authority) VALUES (?, ?)", id, authority);
        }
        return id;
    }

    private static String randomCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String codeChallengeS256(String verifier) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private String authorizeUri(String codeChallenge) {
        return UriComponentsBuilder.fromPath("/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", bffClient.clientId())
                .queryParam("redirect_uri", bffClient.redirectUri())
                .queryParam("scope", "openid")
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build().toUriString();
    }
}
