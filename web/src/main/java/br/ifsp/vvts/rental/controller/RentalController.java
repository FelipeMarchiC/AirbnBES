package br.ifsp.vvts.rental.controller;

import br.ifsp.application.rental.find.FindRentalService;
import br.ifsp.application.rental.update.OwnerUpdateRentalService;
import br.ifsp.vvts.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/rental")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
@Tag(name = "Rental Routes")
public class RentalController {
    private final AuthenticationInfoService authService;
    private final OwnerUpdateRentalService ownerUpdateRentalService;

    private final FindRentalService findRentalService;

    @GetMapping
    public ResponseEntity<String> FindRental() {
        final UUID userId = authService.getAuthenticatedUserId();
        findRentalService.getRentalHistoryByTenant(userId);

        return ResponseEntity.ok("Hello: " + userId.toString());
    }

    @PostMapping("/{rentalId}/confirm")
    public ResponseEntity<?> confirmRental(@PathVariable UUID rentalId) {
        UUID ownerId = authService.getAuthenticatedUserId();
        ownerUpdateRentalService.confirmRental(rentalId, ownerId);
        return ResponseEntity.ok("Rental confirmed successfully.");
    }


}
