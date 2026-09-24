package com.mlcdev.realestate.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${real-estate.bff.frontend-url}")
    private String frontendURL;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository){
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/properties/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/bff/session").permitAll()
                .anyRequest().authenticated()
        );
        http.exceptionHandling(handler -> handler
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        http.requestCache(RequestCacheConfigurer::disable);
        DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        http.oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(resolver))
                .defaultSuccessUrl(frontendURL, true)
        );
        return http.build();
    }
}
