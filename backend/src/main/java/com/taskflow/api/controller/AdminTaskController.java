package com.taskflow.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.api.dto.PageResponse;
import com.taskflow.api.dto.TaskResponse;
import com.taskflow.api.dto.UserTaskStatsResponse;
import com.taskflow.api.service.AdminTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administration")
public class AdminTaskController {

	private final AdminTaskService adminTaskService;

	@GetMapping("/tasks")
	@Operation(summary = "Liste paginée de toutes les tâches, filtrable par propriétaire")
	public PageResponse<TaskResponse> list(@RequestParam(required = false) String ownerId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sort,
			@RequestParam(defaultValue = "desc") String direction) {
		return adminTaskService.list(ownerId, PageRequests.forTasks(page, size, sort, direction));
	}

	@DeleteMapping("/tasks/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Suppression de n'importe quelle tâche")
	public void delete(@PathVariable UUID id) {
		adminTaskService.delete(id);
	}

	@GetMapping("/stats")
	@Operation(summary = "Nombre de tâches par utilisateur et par statut")
	public List<UserTaskStatsResponse> stats() {
		return adminTaskService.stats();
	}
}
