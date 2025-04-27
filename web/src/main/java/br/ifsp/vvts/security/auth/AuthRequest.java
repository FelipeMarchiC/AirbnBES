package br.ifsp.vvts.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthRequest(
        @Schema(description = "Email to be used as login", example = "know.nothing@snow.com")
        String username,
        @Schema(description = "Password", example = "n3243#kFdj$")
        String password
) { }
