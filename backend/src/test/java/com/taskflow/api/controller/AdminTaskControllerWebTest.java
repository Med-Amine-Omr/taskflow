package com.taskflow.api.controller;

import static com.taskflow.api.support.TestJwts.ALICE_ID;
import static com.taskflow.api.support.TestJwts.alice;
import static com.taskflow.api.support.TestJwts.bob;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.taskflow.api.dto.UserTaskStatsResponse;
import com.taskflow.api.exception.ApiErrorWriter;
import com.taskflow.api.security.RestAccessDeniedHandler;
import com.taskflow.api.security.RestAuthenticationEntryPoint;
import com.taskflow.api.security.SecurityConfig;
import com.taskflow.api.service.AdminTaskService;
import com.taskflow.api.support.TestData;

@WebMvcTest(AdminTaskController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
         RestAccessDeniedHandler.class, ApiErrorWriter.class})
@ActiveProfiles("test")
class AdminTaskControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminTaskService adminTaskService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("401 : liste admin sans jeton")
    void list_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(adminTaskService);
    }

    @Test
    @DisplayName("403 : alice (user sans admin) sur la liste admin")
    void list_asSimpleUser_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/tasks").with(alice()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.path").value("/api/admin/tasks"));
        verifyNoInteractions(adminTaskService);
    }

    @Test
    @DisplayName("403 : alice ne peut supprimer aucune tâche via l'admin")
    void delete_asSimpleUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/admin/tasks/{id}", UUID.randomUUID()).with(alice()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(adminTaskService);
    }

    @Test
    @DisplayName("403 : alice sur les statistiques admin")
    void stats_asSimpleUser_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/stats").with(alice()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("200 : bob (admin) liste toutes les tâches, avec filtre propriétaire")
    void list_asAdmin_returns200() throws Exception {
        given(adminTaskService.list(eq(ALICE_ID), any())).willReturn(TestData.emptyPage());

        mockMvc.perform(get("/api/admin/tasks").param("ownerId", ALICE_ID).with(bob()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        verify(adminTaskService).list(eq(ALICE_ID), any());
    }

    @Test
    @DisplayName("204 : bob (admin) supprime n'importe quelle tâche")
    void delete_asAdmin_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/tasks/{id}", id).with(bob()))
                .andExpect(status().isNoContent());
        verify(adminTaskService).delete(id);
    }

    @Test
    @DisplayName("200 : bob (admin) consulte les statistiques par utilisateur")
    void stats_asAdmin_returns200() throws Exception {
        given(adminTaskService.stats()).willReturn(List.of(
                new UserTaskStatsResponse(ALICE_ID, "alice", 3, 1, 1, 1)));

        mockMvc.perform(get("/api/admin/stats").with(bob()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerUsername").value("alice"))
                .andExpect(jsonPath("$[0].total").value(3));
    }
}
