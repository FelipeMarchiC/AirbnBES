package br.ifsp.vvts.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record RegisterUserResponse(
        @Schema(description = "ID of the registered user.", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id
) { }
