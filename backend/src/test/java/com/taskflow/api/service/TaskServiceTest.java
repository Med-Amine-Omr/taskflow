package com.taskflow.api.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskflow.api.dto.TaskRequest;
import com.taskflow.api.dto.TaskStatsResponse;
import com.taskflow.api.entity.Task;
import com.taskflow.api.entity.TaskPriority;
import com.taskflow.api.exception.ResourceNotFoundException;
import com.taskflow.api.mapper.TaskMapper;
import com.taskflow.api.repository.TaskRepository;
import com.taskflow.api.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), ZoneId.of("Africa/Tunis"));

    @Mock
    private TaskRepository taskRepository;

    private TaskService service;

    private final AuthenticatedUser alice =
            new AuthenticatedUser("alice-id", "alice", "Alice Martin", "alice@taskflow.tn", List.of("user"));

    @BeforeEach
    void setUp() {
        service = new TaskService(taskRepository, new TaskMapper(clock), clock);
    }

    @Test
    @DisplayName("Statistiques : en cours = total - terminées - en retard")
    void stats_splitsIntoCompletedInProgressAndOverdue() {
        given(taskRepository.countByOwnerId("alice-id")).willReturn(10L);
        given(taskRepository.countByOwnerIdAndCompletedTrue("alice-id")).willReturn(4L);
        given(taskRepository.countByOwnerIdAndCompletedFalseAndDueDateBefore("alice-id", TODAY)).willReturn(2L);

        assertThat(service.stats(alice)).isEqualTo(new TaskStatsResponse(4, 4, 2, 10));
    }

    @Test
    @DisplayName("Création : propriétaire issu du jeton, statut non terminé")
    void create_setsOwnerFromTokenAndNotCompleted() {
        given(taskRepository.saveAndFlush(any(Task.class))).willAnswer(invocation -> invocation.getArgument(0));

        service.create(alice, new TaskRequest("Préparer la démo", null, TaskPriority.HIGH, null));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).saveAndFlush(captor.capture());
        Task saved = captor.getValue();
        assertThat(saved.getOwnerId()).isEqualTo("alice-id");
        assertThat(saved.getOwnerUsername()).isEqualTo("alice");
        assertThat(saved.isCompleted()).isFalse();
        assertThat(saved.getTitle()).isEqualTo("Préparer la démo");
    }

    @Test
    @DisplayName("Isolation : la tâche d'un autre utilisateur est introuvable")
    void get_taskOfAnotherUser_throwsNotFound() {
        UUID id = UUID.randomUUID();
        given(taskRepository.findByIdAndOwnerId(id, "alice-id")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(alice, id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Toggle : le statut est inversé")
    void toggle_invertsCompleted() {
        UUID id = UUID.randomUUID();
        Task task = new Task();
        task.setId(id);
        task.setTitle("Tâche");
        task.setPriority(TaskPriority.NORMAL);
        task.setCompleted(false);
        given(taskRepository.findByIdAndOwnerId(id, "alice-id")).willReturn(Optional.of(task));
        given(taskRepository.saveAndFlush(task)).willReturn(task);

        assertThat(service.toggle(alice, id).completed()).isTrue();
    }
}
