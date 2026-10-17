package com.taskflow.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.taskflow.api.dto.TaskRequest;
import com.taskflow.api.entity.Task;
import com.taskflow.api.entity.TaskPriority;

class TaskMapperTest {

    // "Aujourd'hui" est figé au 15 octobre 2026, à Paris.
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), ZoneId.of("Africa/Tunis"));
    private final TaskMapper mapper = new TaskMapper(clock);

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    private Task task(LocalDate dueDate, boolean completed) {
        Task task = new Task();
        task.setTitle("Tâche");
        task.setPriority(TaskPriority.NORMAL);
        task.setDueDate(dueDate);
        task.setCompleted(completed);
        return task;
    }

    @Test
    @DisplayName("En retard : non terminée et échéance passée")
    void overdue_whenNotCompletedAndDueDateInPast() {
        assertThat(mapper.toResponse(task(TODAY.minusDays(1), false)).overdue()).isTrue();
    }

    @Test
    @DisplayName("Pas en retard : échéance aujourd'hui")
    void notOverdue_whenDueToday() {
        assertThat(mapper.toResponse(task(TODAY, false)).overdue()).isFalse();
    }

    @Test
    @DisplayName("Pas en retard : tâche terminée, même avec une échéance passée")
    void notOverdue_whenCompleted() {
        assertThat(mapper.toResponse(task(TODAY.minusDays(10), true)).overdue()).isFalse();
    }

    @Test
    @DisplayName("Pas en retard : pas d'échéance")
    void notOverdue_whenNoDueDate() {
        assertThat(mapper.toResponse(task(null, false)).overdue()).isFalse();
    }

    @Test
    @DisplayName("Le titre est nettoyé et une description vide devient null")
    void apply_trimsTitleAndNormalizesDescription() {
        Task task = new Task();
        mapper.apply(new TaskRequest("  Mon titre  ", "   ", TaskPriority.HIGH, null), task);

        assertThat(task.getTitle()).isEqualTo("Mon titre");
        assertThat(task.getDescription()).isNull();
        assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
    }
}
