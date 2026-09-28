package com.mlcdev.realestate.auth.config;

import com.mlcdev.realestate.auth.config.properties.BffClientProperties;
import com.mlcdev.realestate.auth.config.properties.JwtKeyProperties;
import com.mlcdev.realestate.auth.config.properties.PostmanClientProperties;
import com.mlcdev.realestate.auth.config.properties.SwaggerClientProperties;
import com.mlcdev.realestate.auth.config.properties.TokenDurationProperties;
import com.mlcdev.realestate.auth.entities.AuthUserEntity;
import com.mlcdev.realestate.auth.repositories.AuthUserEntityRepository;
import com.mlcdev.realestate.auth.security.CustomUserDetails;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthUserEntityRepository repository;

    @Value("${security.authorization-server.url}")
    private String authorizationServerUrl;

    public SecurityConfig(AuthUserEntityRepository repository) {
        this.repository = repository;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http){

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher()).with(authorizationServerConfigurer, as -> as.oidc(Customizer.withDefaults())).authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated()).exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, SessionRegistry sessionRegistry){
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(-1)
                                .sessionRegistry(sessionRegistry)
                        ));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        DelegatingPasswordEncoder encoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();

        encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        return encoder;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            AuthUserEntity authUserEntity = repository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
            return new CustomUserDetails(authUserEntity);
        };
    }

    @Bean
    @Profile("dev")
    public RegisteredClientRepository devRegisteredClientRepository(BffClientProperties bffClientProperties, PostmanClientProperties postmanClientProperties, SwaggerClientProperties swaggerClientProperties, TokenDurationProperties tokenDurationProperties, PasswordEncoder passwordEncoder) {
        return new InMemoryRegisteredClientRepository(createBffClient(bffClientProperties, tokenDurationProperties, passwordEncoder), createPostmanClient(postmanClientProperties, tokenDurationProperties, passwordEncoder), createSwaggerClient(swaggerClientProperties, tokenDurationProperties));
    }

    @Bean
    @Profile("prod")
    public RegisteredClientRepository prodRegisteredClientRepository(BffClientProperties bffClientProperties, TokenDurationProperties tokenDurationProperties, PasswordEncoder passwordEncoder) {
        return new InMemoryRegisteredClientRepository(createBffClient(bffClientProperties, tokenDurationProperties, passwordEncoder));
    }

    private RegisteredClient createBffClient(BffClientProperties properties, TokenDurationProperties tokenDurationProperties, PasswordEncoder passwordEncoder) {
        return RegisteredClient.withId(UUID.nameUUIDFromBytes(properties.clientId().getBytes(StandardCharsets.UTF_8)).toString()).clientId(properties.clientId()).clientSecret(passwordEncoder.encode(properties.clientSecret())).clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC).authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN).redirectUri(properties.redirectUri()).postLogoutRedirectUri(properties.postLogoutRedirectUri()).scope(OidcScopes.OPENID).clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build()).tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofSeconds(tokenDurationProperties.access())).refreshTokenTimeToLive(Duration.ofSeconds(tokenDurationProperties.refresh())).reuseRefreshTokens(false).build()).build();
    }

    private RegisteredClient createPostmanClient(PostmanClientProperties properties, TokenDurationProperties tokenDurationProperties, PasswordEncoder passwordEncoder) {
        return RegisteredClient.withId(UUID.nameUUIDFromBytes(properties.clientId().getBytes(StandardCharsets.UTF_8)).toString()).clientId(properties.clientId()).clientSecret(passwordEncoder.encode(properties.clientSecret())).clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC).authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN).redirectUri(properties.redirectUri()).scope(OidcScopes.OPENID).clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build()).tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofSeconds(tokenDurationProperties.access())).refreshTokenTimeToLive(Duration.ofSeconds(tokenDurationProperties.refresh())).reuseRefreshTokens(false).build()).build();
    }

    private RegisteredClient createSwaggerClient(SwaggerClientProperties properties, TokenDurationProperties tokenDurationProperties) {
        return RegisteredClient.withId(UUID.nameUUIDFromBytes(properties.clientId().getBytes(StandardCharsets.UTF_8)).toString())
                .clientId(properties.clientId())
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(properties.redirectUri())
                .scope(OidcScopes.OPENID)
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofSeconds(tokenDurationProperties.access()))
                        .build())
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            OAuth2TokenType tokenType = context.getTokenType();
            boolean isAccessToken = tokenType.equals(OAuth2TokenType.ACCESS_TOKEN);
            boolean isIdToken = tokenType.getValue().endsWith(OidcParameterNames.ID_TOKEN);
            if(!isAccessToken && !isIdToken){
                return;
            }
            Authentication principal = context.getPrincipal();
            if (!(principal.getPrincipal() instanceof CustomUserDetails user)) {
                return;
            }
            List<String> authorities = user.getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toCollection(ArrayList::new));

            context.getClaims().claim("authorities", authorities);

            if (isAccessToken) {
                context.getClaims().subject(user.getId().toString()).claim("username", user.getUsername());
            }
        };
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(JwtKeyProperties keyProperties) {
        String keyId = Base64.getUrlEncoder().withoutPadding().encodeToString(keyProperties.publicKey().getEncoded()).substring(0, 16);
        RSAKey rsaKey = new RSAKey.Builder(keyProperties.publicKey()).privateKey(keyProperties.privateKey()).keyID(keyId).build();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().issuer(authorizationServerUrl).build();
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        JdbcOAuth2AuthorizationService service = new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
        BasicPolymorphicTypeValidator.Builder typeValidatorBuilder = BasicPolymorphicTypeValidator.builder().allowIfSubType(CustomUserDetails.class);
        JsonMapper.Builder jsonMapperBuilder = JsonMapper.builder();
        List<JacksonModule> securityModules = SecurityJacksonModules.getModules(classLoader, typeValidatorBuilder);
        jsonMapperBuilder.addModules(securityModules);
        jsonMapperBuilder.addModule(new OAuth2AuthorizationServerJacksonModule());
        JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper rowMapper = new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(registeredClientRepository, jsonMapperBuilder.build());
        service.setAuthorizationRowMapper(rowMapper);
        return service;

    }

    @Bean
    public SessionRegistry sessionRegistry(FindByIndexNameSessionRepository<? extends Session> sessionRepository){
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }
}
