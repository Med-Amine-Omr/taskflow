package com.taskflow.api.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.taskflow.api.dto.PageResponse;
import com.taskflow.api.dto.TaskRequest;
import com.taskflow.api.dto.TaskResponse;
import com.taskflow.api.dto.TaskStatsResponse;
import com.taskflow.api.entity.TaskPriority;
import com.taskflow.api.security.AuthenticatedUser;
import com.taskflow.api.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Mes tâches")
public class TaskController {

	private final TaskService taskService;

	@GetMapping
	@Operation(summary = "Liste paginée de mes tâches, filtrable par statut et priorité")
	public PageResponse<TaskResponse> list(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) Boolean completed, @RequestParam(required = false) TaskPriority priority,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sort,
			@RequestParam(defaultValue = "desc") String direction) {
		return taskService.list(AuthenticatedUser.from(jwt), completed, priority,
				PageRequests.forTasks(page, size, sort, direction));
	}

	@GetMapping("/stats")
	@Operation(summary = "Nombre de tâches terminées, en cours et en retard")
	public TaskStatsResponse stats(@AuthenticationPrincipal Jwt jwt) {
		return taskService.stats(AuthenticatedUser.from(jwt));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Détail d'une de mes tâches")
	public TaskResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return taskService.get(AuthenticatedUser.from(jwt), id);
	}

	@PostMapping
	@Operation(summary = "Création d'une tâche")
	public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody TaskRequest request) {
		TaskResponse created = taskService.create(AuthenticatedUser.from(jwt), request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Modification d'une de mes tâches")
	public TaskResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody TaskRequest request) {
		return taskService.update(AuthenticatedUser.from(jwt), id, request);
	}

	@PatchMapping("/{id}/toggle")
	@Operation(summary = "Changement du statut d'une de mes tâches")
	public TaskResponse toggle(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return taskService.toggle(AuthenticatedUser.from(jwt), id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Suppression d'une de mes tâches")
	public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		taskService.delete(AuthenticatedUser.from(jwt), id);
	}
}
