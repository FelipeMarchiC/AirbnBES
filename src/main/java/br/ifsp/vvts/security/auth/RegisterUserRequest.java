package br.ifsp.vvts.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterUserRequest(
        @Schema(description = "Name", example = "John")
        String name,
        @Schema(description = "Lastname", example = "Snow")
        String lastname,
        @Schema(description = "Email to be used as login", example = "know.nothing@snow.com")
        String email,
        @Schema(description = "Password", example = "n3243#kFdj$")
        String password
) {}
