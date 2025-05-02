package br.ifsp.vvts.rental.controller;

import br.ifsp.application.rental.find.FindRentalService;
import br.ifsp.vvts.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/rental")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
@Tag(name = "Rental Routes")
public class RentalController {
    private final AuthenticationInfoService authService;
    private final FindRentalService findRentalService;

    @GetMapping
    public ResponseEntity<String> FindRental() {
        final UUID userId = authService.getAuthenticatedUserId();
        findRentalService.getRentalHistoryByTenant(userId);

        return ResponseEntity.ok("Hello: " + userId.toString());
    }

}
