package com.taskflow.api.security;

import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		return KeycloakRoles.extract(jwt).stream()
				.map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role)).toList();
	}
}
