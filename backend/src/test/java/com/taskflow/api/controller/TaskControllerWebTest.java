package com.taskflow.api.controller;

import static com.taskflow.api.support.TestJwts.ALICE_ID;
import static com.taskflow.api.support.TestJwts.alice;
import static com.taskflow.api.support.TestJwts.withRoles;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.taskflow.api.dto.TaskStatsResponse;
import com.taskflow.api.exception.ApiErrorWriter;
import com.taskflow.api.security.AuthenticatedUser;
import com.taskflow.api.security.RestAccessDeniedHandler;
import com.taskflow.api.security.RestAuthenticationEntryPoint;
import com.taskflow.api.security.SecurityConfig;
import com.taskflow.api.service.TaskService;
import com.taskflow.api.support.TestData;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
         RestAccessDeniedHandler.class, ApiErrorWriter.class})
@ActiveProfiles("test")
class TaskControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // ===================== 401 =====================

    @Test
    @DisplayName("401 : requête sans jeton, au format d'erreur unique")
    void list_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/tasks"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors").isArray());
        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("401 : jeton invalide")
    void list_withInvalidToken_returns401() throws Exception {
        willThrow(new BadJwtException("Signature invalide")).given(jwtDecoder).decode("jeton-invalide");

        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer jeton-invalide"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(taskService);
    }

    // ===================== 403 =====================

    @Test
    @DisplayName("403 : utilisateur authentifié sans le rôle user")
    void list_withoutUserRole_returns403() throws Exception {
        mockMvc.perform(get("/api/tasks").with(withRoles("carol-id", "carol")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.path").value("/api/tasks"));
        verifyNoInteractions(taskService);
    }

    // ===================== 200 / 201 =====================

    @Test
    @DisplayName("200 : alice (rôle user) liste ses tâches")
    void list_asUser_returns200() throws Exception {
        given(taskService.list(any(), any(), any(), any())).willReturn(TestData.emptyPage());

        mockMvc.perform(get("/api/tasks").with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("200 : les statistiques personnelles sont accessibles")
    void stats_asUser_returns200() throws Exception {
        given(taskService.stats(any())).willReturn(new TaskStatsResponse(1, 2, 3, 6));

        mockMvc.perform(get("/api/tasks/stats").with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.inProgress").value(2))
                .andExpect(jsonPath("$.overdue").value(3));
    }

    @Test
    @DisplayName("201 : création d'une tâche avec en-tête Location")
    void create_asUser_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        given(taskService.create(any(), any())).willReturn(TestData.task(id, "Préparer la démo"));

        mockMvc.perform(post("/api/tasks").with(alice())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Préparer la démo", "priority": "HIGH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/tasks/" + id)))
                .andExpect(jsonPath("$.title").value("Préparer la démo"));
    }

    @Test
    @DisplayName("Le propriétaire transmis au service vient du jeton")
    void create_passesOwnerFromToken() throws Exception {
        given(taskService.create(any(), any())).willReturn(TestData.task(UUID.randomUUID(), "x"));

        mockMvc.perform(post("/api/tasks").with(alice())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "x", "priority": "LOW", "ownerId": "bob-id"}
                                """))
                .andExpect(status().isCreated());

        verify(taskService).create(
                eq(new AuthenticatedUser(ALICE_ID, "alice", "alice", "alice@taskflow.tn", List.of("user"))),
                any());
    }

    // ===================== 400 (validation et format d'erreur) =====================

    @Test
    @DisplayName("400 : titre vide, erreur sur le champ title")
    void create_withBlankTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks").with(alice())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "priority": "NORMAL"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("400 : titre de plus de 100 caractères")
    void create_withTooLongTitle_returns400() throws Exception {
        String body = """
                {"title": "%s", "priority": "NORMAL"}
                """.formatted("a".repeat(101));

        mockMvc.perform(post("/api/tasks").with(alice())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
    }

    @Test
    @DisplayName("400 : priorité inconnue, erreur sur le champ priority")
    void create_withUnknownPriority_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks").with(alice())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Ok", "priority": "URGENT"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("priority"));
    }

    @Test
    @DisplayName("400 : tri non autorisé")
    void list_withForbiddenSort_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("sort", "title").with(alice()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("400 : identifiant qui n'est pas un UUID")
    void get_withInvalidId_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks/abc").with(alice()))
                .andExpect(status().isBadRequest());
    }
}
