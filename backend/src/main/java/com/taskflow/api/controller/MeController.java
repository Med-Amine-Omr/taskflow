package com.taskflow.api.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.api.dto.MeResponse;
import com.taskflow.api.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/me")
@Tag(name = "Utilisateur")
public class MeController {

    @GetMapping
    @Operation(summary = "Nom, email et rôles de l'utilisateur connecté")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        return new MeResponse(user.id(), user.username(), user.name(), user.email(), user.roles());
    }
}
