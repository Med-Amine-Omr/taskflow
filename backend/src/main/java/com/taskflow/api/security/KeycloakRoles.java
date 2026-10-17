package com.taskflow.api.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.jwt.Jwt;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class KeycloakRoles {

	public static List<String> extract(Jwt jwt) {

		Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
		if (realmAccess == null) {
			return List.of();
		}

		Object roles = realmAccess.get("roles");
		if (roles instanceof Collection<?> collection) {
			return collection.stream().map(Object::toString).toList();
		}

		return List.of();
	}
}
