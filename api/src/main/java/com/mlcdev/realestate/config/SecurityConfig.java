package com.mlcdev.realestate.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mlcdev.realestate.entities.Role;
import com.mlcdev.realestate.entities.User;
import com.mlcdev.realestate.repository.UserRepository;
import com.mlcdev.realestate.security.CustomUserDetails;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;
    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;
    @Value("${security.authorization-server.url}")
    private String authorizationServerUrl;
    @Value("${security.clients.spa.client-id:}")
    private String spaClientId;
    @Value("${security.clients.spa.redirect-uri:}")
    private String spaRedirectUri;
    @Value("${security.cors.allowed-origins}")
    private String allowedOrigins;
    @Value("${security.clients.postman.client-id:}")
    private String postmanClientId;
    @Value("${security.clients.postman.redirect-uri:}")
    private String postmanRedirectUri;
    @Value("${security.jwt.access-token-duration}")
    private Integer accessTokenSecondsDuration;
    @Value("${security.jwt.refresh-token-duration}")
    private Integer refreshTokenSecondsDuration;
    @Value("${security.clients.swagger.client-id}")
    private String swaggerClientId;
    @Value("${security.clients.swagger.redirect-uri}")
    private String swaggerRedirectUri;
    @Value("${security.logout.redirect-url}")
    private String logoutRedirectUrl;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher()).cors(Customizer.withDefaults()).with(authorizationServerConfigurer, configurer -> configurer.oidc(Customizer.withDefaults())).authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated()).exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(authorize -> authorize.requestMatchers(HttpMethod.POST, "/login").permitAll().requestMatchers(HttpMethod.GET, "/v1/properties/**").permitAll().requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll().requestMatchers(HttpMethod.GET, "/actuator/health").permitAll().anyRequest().authenticated()).oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwkSetUri(authorizationServerUrl + "/oauth2/jwks").jwtAuthenticationConverter(jwtAuthenticationConverter()))).formLogin(Customizer.withDefaults()).logout(logout -> logout.logoutSuccessUrl(logoutRedirectUrl).permitAll());

        return http.build();
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {

        JdbcOAuth2AuthorizationService service = new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);

        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();

        BasicPolymorphicTypeValidator.Builder typeValidatorBuilder = BasicPolymorphicTypeValidator.builder().allowIfSubType(CustomUserDetails.class).allowIfSubType(Role.class);

        JsonMapper.Builder jsonMapperBuilder = JsonMapper.builder();

        List<JacksonModule> securityModules = SecurityJacksonModules.getModules(classLoader, typeValidatorBuilder);

        jsonMapperBuilder.addModules(securityModules);
        jsonMapperBuilder.addModule(new OAuth2AuthorizationServerJacksonModule());

        JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper rowMapper = new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(registeredClientRepository, jsonMapperBuilder.build());

        service.setAuthorizationRowMapper(rowMapper);

        return service;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return CustomUserDetails.builder().id(user.getId()).username(user.getUsername()).password(user.getPassword()).active(user.isActive()).authorities(new HashSet<>(user.getAuthorities())).build();
        };
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
                Authentication principal = context.getPrincipal();
                if (!(principal.getPrincipal() instanceof CustomUserDetails user)) {
                    return;
                }
                List<String> authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toCollection(ArrayList::new));
                context.getClaims().subject(user.getId().toString()).claim("authorities", authorities).claim("username", user.getUsername());
            }
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(origin -> !origin.isBlank()).toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        configuration.setExposedHeaders(List.of("Location"));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Profile("dev")
    @Bean
    public RegisteredClientRepository devRegisteredClientRepository() {
        return new InMemoryRegisteredClientRepository(buildClient(postmanClientId, postmanRedirectUri), buildClient(swaggerClientId, swaggerRedirectUri));
    }

    @Profile("prod")
    @Bean
    public RegisteredClientRepository prodRegisteredClientRepository() {
        return new InMemoryRegisteredClientRepository(buildClient(spaClientId, spaRedirectUri));
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        String keyId = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getEncoded()).substring(0, 16);
        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(keyId).build();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().issuer(authorizationServerUrl).build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix().role("ADMIN").implies("BROKER").build();
    }


    private RegisteredClient buildClient(String clientId, String clientRedirectUri) {
        return RegisteredClient.withId(UUID.nameUUIDFromBytes(clientId.getBytes()).toString()).clientId(clientId).clientAuthenticationMethod(ClientAuthenticationMethod.NONE).authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN).redirectUri(clientRedirectUri).scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE).clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build()).tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofSeconds(accessTokenSecondsDuration)).refreshTokenTimeToLive(Duration.ofSeconds(refreshTokenSecondsDuration)).reuseRefreshTokens(false).build()).build();
    }
}
