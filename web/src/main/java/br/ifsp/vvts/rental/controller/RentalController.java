package br.ifsp.vvts.rental.controller;

import br.ifsp.application.rental.delete.DeleteRentalService;
import br.ifsp.application.rental.find.FindRentalService;
import br.ifsp.application.rental.update.OwnerUpdateRentalService;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.vvts.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/rental")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
@Tag(name = "Rental Routes")
public class RentalController {
    private final AuthenticationInfoService authService;
    private final OwnerUpdateRentalService ownerUpdateRentalService;
    private final DeleteRentalService deleteRentalService;

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

    @PostMapping("/{rentalId}/deny")
    public ResponseEntity<?> denyRental(@PathVariable UUID rentalId) {
        Rental rental = ownerUpdateRentalService.getRentalById(rentalId);
        ownerUpdateRentalService.deny(rental);
        return ResponseEntity.ok("Rental denied successfully.");
    }

    @PostMapping("/{rentalId}/cancel")
    public ResponseEntity<?> cancelRental(@PathVariable UUID rentalId,
                                          @RequestParam(required = false) String cancelDate) {
        Rental rental = ownerUpdateRentalService.getRentalById(rentalId);
        LocalDate parsedDate = cancelDate != null ? LocalDate.parse(cancelDate) : null;
        ownerUpdateRentalService.cancel(rental, parsedDate);
        return ResponseEntity.ok("Rental cancelled successfully.");
    }

    @DeleteMapping("/{rentalId}")
    public ResponseEntity<?> deleteRental(@PathVariable UUID rentalId) {
        Rental rental = deleteRentalService.getRentalById(rentalId);
        UUID deletedId = deleteRentalService.delete(rental);
        return ResponseEntity.ok("Rental deleted successfully: " + deletedId);
    }

}
