package br.ifsp.vvts.rental.controller;

import br.ifsp.vvts.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173/")
@RestController
@RequestMapping(path = "/api/v1/hello")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
@Tag(name = "Hello API")
public class TransactionController {

    private final AuthenticationInfoService authService;

    @GetMapping
    public ResponseEntity<String> hello() {
        final UUID userId = authService.getAuthenticatedUserId();
        return ResponseEntity.ok("Hello: " + userId.toString());
    }
}
