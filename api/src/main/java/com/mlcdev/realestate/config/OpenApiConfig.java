package com.mlcdev.realestate.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${security.authorization-server.url}")
    private String authorizationServerUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Real Estate API")
                        .description("""
                                API for property management
                                
                                ## Authentication
                                Click **Authorize** to login. To switch users:
                                1. Click **Logout** in the Authorize dialog
                                2. [Clear server session](%s/logout)
                                3. Click **Authorize** again to login with another user
                                
                                """.formatted(authorizationServerUrl))
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(authorizationServerUrl + "/oauth2/authorize")
                                                .tokenUrl(authorizationServerUrl + "/oauth2/token")
                                                .scopes(new Scopes()
                                                        .addString("openid", "OpenID")
                                                        .addString("profile", "Profile"))))));
    }
}
