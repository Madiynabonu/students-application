package com.application.students.studentsapplication.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    @Nullable
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractRoles(jwt);

        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
    }


    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return List.of();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");

        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());


    }
}

















/**

 Add student CRUD API with Keycloak-secured OAuth2 resource serve
    - Student entity/DTO/repository/service/controller: full CRUD (create, read all, read by id, update, delete)
    - Request/response DTOs correctly separated: StudentRequestDTO for input validation (@NotBlank), StudentResponseDTO for output
    - Student.id auto-generated via @GeneratedValue(IDENTITY)
    - Global exception handling for not-found and validation errors


* */