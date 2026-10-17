package com.taskflow.api.controller;

import static com.taskflow.api.support.TestJwts.ALICE_ID;
import static com.taskflow.api.support.TestJwts.BOB_ID;
import static com.taskflow.api.support.TestJwts.alice;
import static com.taskflow.api.support.TestJwts.withRoles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.taskflow.api.exception.ApiErrorWriter;
import com.taskflow.api.security.RestAccessDeniedHandler;
import com.taskflow.api.security.RestAuthenticationEntryPoint;
import com.taskflow.api.security.SecurityConfig;

@WebMvcTest(MeController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
         RestAccessDeniedHandler.class, ApiErrorWriter.class})
@ActiveProfiles("test")
class MeControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("401 : /api/me sans jeton")
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("200 : alice voit son nom, son email et son rôle user")
    void me_asAlice_returns200() throws Exception {
        mockMvc.perform(get("/api/me").with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ALICE_ID))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@taskflow.tn"))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("user"));
    }

    @Test
    @DisplayName("200 : bob voit ses deux rôles, sans les rôles techniques Keycloak")
    void me_asBob_returnsBothRoles() throws Exception {
        mockMvc.perform(get("/api/me").with(withRoles(BOB_ID, "bob", "user", "admin", "offline_access")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles.length()").value(2))
                .andExpect(jsonPath("$.roles[0]").value("admin"))
                .andExpect(jsonPath("$.roles[1]").value("user"));
    }
}
