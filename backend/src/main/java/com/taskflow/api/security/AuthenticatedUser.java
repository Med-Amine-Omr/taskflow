package com.taskflow.api.security;

import java.util.List;
import java.util.Set;

import org.springframework.security.oauth2.jwt.Jwt;

public record AuthenticatedUser(String id, String username, String name, String email, List<String> roles) {

	private static final Set<String> APPLICATION_ROLES = Set.of("user", "admin");

	public static AuthenticatedUser from(Jwt jwt) {

		List<String> roles = KeycloakRoles.extract(jwt).stream().filter(APPLICATION_ROLES::contains).sorted().toList();

		return new AuthenticatedUser(jwt.getSubject(), jwt.getClaimAsString("preferred_username"),
				jwt.getClaimAsString("name"), jwt.getClaimAsString("email"), roles);
	}
}
