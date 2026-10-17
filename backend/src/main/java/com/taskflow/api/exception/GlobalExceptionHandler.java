package com.taskflow.api.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		List<ApiError.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
				.map(f -> new ApiError.FieldError(f.getField(), f.getDefaultMessage())).toList();
		return build(HttpStatus.BAD_REQUEST, "Données invalides", request, fields);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
		if (ex.getCause() instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
			String field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
			return build(HttpStatus.BAD_REQUEST, "Données invalides", request,
					List.of(new ApiError.FieldError(field, "Valeur invalide")));
		}
		return build(HttpStatus.BAD_REQUEST, "Corps de requête illisible", request, List.of());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Paramètre invalide : " + ex.getName(), request, List.of());
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "Ressource introuvable", request, List.of());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiError> handleMethod(HttpRequestMethodNotSupportedException ex,
			HttpServletRequest request) {
		return build(HttpStatus.METHOD_NOT_ALLOWED, "Méthode non autorisée", request, List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Erreur inattendue sur {}", request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", request, List.of());
	}

	private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request,
			List<ApiError.FieldError> fields) {
		return ResponseEntity.status(status).body(ApiError.of(status, message, request.getRequestURI(), fields));
	}
}
