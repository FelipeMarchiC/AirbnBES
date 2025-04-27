package br.ifsp.vvts.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(
                description = "JTW credential",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRkZm9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzQxMDYzNzIzLCJleHAiOjE3NDEwNzIzNjN9.EYAEc92Bjf6brHRYGN3VJaGk9Nuy8mMb141cXxmfel4"
        )
        String token
){ }
