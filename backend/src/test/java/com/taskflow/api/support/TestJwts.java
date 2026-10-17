package com.taskflow.api.support;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import com.taskflow.api.security.KeycloakRealmRoleConverter;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TestJwts {

    public static final String ALICE_ID = "alice-id";
    public static final String BOB_ID = "bob-id";

    /** alice : rôle user uniquement (comme dans le realm). */
    public static JwtRequestPostProcessor alice() {
        return withRoles(ALICE_ID, "alice", "user");
    }

    /** bob : rôles user et admin (comme dans le realm). */
    public static JwtRequestPostProcessor bob() {
        return withRoles(BOB_ID, "bob", "user", "admin");
    }

    /** Un utilisateur authentifié avec les rôles de realm donnés (éventuellement aucun). */
    public static JwtRequestPostProcessor withRoles(String subject, String username, String... roles) {
        return jwt()
                .jwt(token -> token
                        .subject(subject)
                        .claim("preferred_username", username)
                        .claim("name", username)
                        .claim("email", username + "@taskflow.tn")
                        .claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(new KeycloakRealmRoleConverter());
    }
}
