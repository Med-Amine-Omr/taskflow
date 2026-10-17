package com.taskflow.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskflowOpenApi(@Value("${app.keycloak.issuer-uri}") String issuerUri) {
        String oidc = issuerUri + "/protocol/openid-connect";
        return new OpenAPI()
                .info(new Info()
                        .title("TaskFlow API")
                        .version("1.0")
                        .description("API de gestion de tâches sécurisée par Keycloak"))
                .components(new Components().addSecuritySchemes("keycloak", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                                .authorizationUrl(oidc + "/auth")
                                .tokenUrl(oidc + "/token")))))
                .addSecurityItem(new SecurityRequirement().addList("keycloak"));
    }
}
