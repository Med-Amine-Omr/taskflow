package com.taskflow.api;

import static com.taskflow.api.support.TestJwts.ALICE_ID;
import static com.taskflow.api.support.TestJwts.alice;
import static com.taskflow.api.support.TestJwts.bob;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.jayway.jsonpath.JsonPath;
import com.taskflow.api.support.AbstractIntegrationTest;

class TaskApiIntegrationTest extends AbstractIntegrationTest {

    // Même fuseau que APP_TIMEZONE dans application-test.yml
    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Africa/Tunis"));

    private String createTask(RequestPostProcessor user, String title, String priority, LocalDate dueDate)
            throws Exception {
        String due = dueDate == null ? "null" : "\"" + dueDate + "\"";
        String body = """
                {"title": "%s", "priority": "%s", "dueDate": %s}
                """.formatted(title, priority, due);

        MvcResult result = mockMvc.perform(post("/api/tasks").with(user)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    @Test
    @DisplayName("Cycle de vie complet : créer, lire, modifier, changer le statut, supprimer")
    void taskLifecycle() throws Exception {
        String id = createTask(alice(), "Préparer la démo", "HIGH", TODAY.plusDays(2));

        mockMvc.perform(get("/api/tasks/{id}", id).with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Préparer la démo"))
                .andExpect(jsonPath("$.ownerUsername").value("alice"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        mockMvc.perform(put("/api/tasks/{id}", id).with(alice())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Démo finale", "description": "Avec slides", "priority": "LOW"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Démo finale"))
                .andExpect(jsonPath("$.priority").value("LOW"));

        mockMvc.perform(patch("/api/tasks/{id}/toggle", id).with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        mockMvc.perform(delete("/api/tasks/{id}", id).with(alice()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", id).with(alice()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Isolation : bob ne peut ni voir, ni modifier, ni supprimer la tâche d'alice")
    void userCannotAccessAnotherUsersTask() throws Exception {
        String aliceTask = createTask(alice(), "Tâche d'alice", "NORMAL", null);

        mockMvc.perform(get("/api/tasks/{id}", aliceTask).with(bob()))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/tasks/{id}", aliceTask).with(bob())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Piratée", "priority": "LOW"}
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/tasks/{id}/toggle", aliceTask).with(bob()))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/tasks/{id}", aliceTask).with(bob()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/tasks").with(bob()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/tasks/{id}", aliceTask).with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tâche d'alice"));
    }

    @Test
    @DisplayName("Statistiques personnelles : terminées, en cours, en retard")
    void personalStats() throws Exception {
        createTask(alice(), "En retard", "HIGH", TODAY.minusDays(1));
        String done = createTask(alice(), "Terminée", "LOW", TODAY.minusDays(5));
        mockMvc.perform(patch("/api/tasks/{id}/toggle", done).with(alice()))
                .andExpect(status().isOk());
        createTask(alice(), "Pour aujourd'hui", "NORMAL", TODAY);
        createTask(alice(), "Sans échéance", "NORMAL", null);
        createTask(bob(), "Tâche de bob en retard", "NORMAL", TODAY.minusDays(1));

        mockMvc.perform(get("/api/tasks/stats").with(alice()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.overdue").value(1))
                .andExpect(jsonPath("$.inProgress").value(2))
                .andExpect(jsonPath("$.total").value(4));
    }

    @Test
    @DisplayName("Liste : filtres statut et priorité, tri par échéance, pagination")
    void listFiltersSortAndPagination() throws Exception {
        createTask(alice(), "B", "HIGH", TODAY.plusDays(5));
        createTask(alice(), "A", "HIGH", TODAY.plusDays(1));
        createTask(alice(), "C", "LOW", TODAY.plusDays(3));

        mockMvc.perform(get("/api/tasks").with(alice())
                        .param("priority", "HIGH").param("sort", "dueDate").param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("A"))
                .andExpect(jsonPath("$.content[1].title").value("B"));

        mockMvc.perform(get("/api/tasks").with(alice()).param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/tasks").with(alice()).param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("Admin : voit, filtre et supprime toutes les tâches, statistiques par utilisateur")
    void adminEndpoints() throws Exception {
        String aliceTask = createTask(alice(), "Tâche d'alice", "HIGH", null);
        createTask(bob(), "Tâche de bob", "LOW", TODAY.minusDays(1));

        mockMvc.perform(get("/api/admin/tasks").with(bob()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/admin/tasks").with(bob()).param("ownerId", ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].ownerUsername").value("alice"));

        mockMvc.perform(get("/api/admin/stats").with(bob()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.ownerUsername == 'bob')].overdue", hasItem(1)));

        mockMvc.perform(delete("/api/admin/tasks/{id}", aliceTask).with(bob()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/tasks/{id}", aliceTask).with(alice()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("403 : alice n'a pas accès à l'administration")
    void adminForbiddenForSimpleUser() throws Exception {
        mockMvc.perform(get("/api/admin/tasks").with(alice()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Health check public et base de données disponible")
    void healthCheckIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
