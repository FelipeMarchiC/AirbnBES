package br.ifsp.vvts.rental.controller;

import br.ifsp.application.rental.create.ICreateRentalService;
import br.ifsp.application.rental.find.FindRentalService;
import br.ifsp.application.rental.update.owner.IOwnerUpdateRentalService;
import br.ifsp.application.rental.update.owner.OwnerUpdateRentalService;
import br.ifsp.application.rental.update.tenant.ITenantUpdateRentalService;
import br.ifsp.vvts.rental.presenter.RestCreateRentalPresenter;
import br.ifsp.vvts.rental.presenter.RestOwnerUpdateRentalPresenter;
import br.ifsp.vvts.rental.presenter.RestTenantUpdateRentalPresenter;
import br.ifsp.vvts.rental.requests.PostRequest;
import br.ifsp.vvts.rental.requests.PutRequest;
import br.ifsp.vvts.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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

    private final ICreateRentalService createRentalService;
    private final FindRentalService findRentalService;
    private final OwnerUpdateRentalService ownerUpdateRentalService;
    private final ITenantUpdateRentalService tenantUpdateRentalService;

    @GetMapping
    public ResponseEntity<String> FindRental() {
        final UUID userId = authService.getAuthenticatedUserId();
        findRentalService.getRentalHistoryByTenant(userId);

        return ResponseEntity.ok("Hello: " + userId.toString());
    }

    @PostMapping
    public ResponseEntity<?> createRental(@RequestBody PostRequest request) {
        var presenter = new RestCreateRentalPresenter();
        var userId = authService.getAuthenticatedUserId();
        var requestModel = request.toCreateRequestModel(userId);

        createRentalService.registerRental(presenter, requestModel);

        return presenter.responseEntity() != null ?
                presenter.responseEntity()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PutMapping("/{rentalId}/confirm")
    public ResponseEntity<?> confirmRental(@PathVariable UUID rentalId) {
        UUID ownerId = authService.getAuthenticatedUserId();
        var presenter = new RestOwnerUpdateRentalPresenter();
        var requestModel = new IOwnerUpdateRentalService.RequestModel(ownerId, rentalId);

        ownerUpdateRentalService.confirmRental(presenter, requestModel);

        return presenter.responseEntity() != null ?
                presenter.responseEntity()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PutMapping("/{rentalId}/deny")
    public ResponseEntity<?> denyRental(@PathVariable UUID rentalId) {
        UUID ownerId = authService.getAuthenticatedUserId();
        var presenter = new RestOwnerUpdateRentalPresenter();
        var requestModel = new IOwnerUpdateRentalService.RequestModel(ownerId, rentalId);

        ownerUpdateRentalService.denyRental(presenter, requestModel);

        return presenter.responseEntity() != null ?
                presenter.responseEntity()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PutMapping("/{rentalId}/cancel")
    public ResponseEntity<?> cancelRental(@PathVariable UUID rentalId,
                                          @RequestParam(required = false) String cancelDate) {
        UUID ownerId = authService.getAuthenticatedUserId();
        LocalDate parsedDate = cancelDate != null ? LocalDate.parse(cancelDate) : null;
        var presenter = new RestOwnerUpdateRentalPresenter();
        var requestModel = new IOwnerUpdateRentalService.RequestModel(ownerId, rentalId);

        ownerUpdateRentalService.cancelRental(presenter, requestModel, parsedDate);

        return presenter.responseEntity() != null ?
                presenter.responseEntity()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PutMapping("/{rentalId}/tenant/cancel")
    public ResponseEntity<?> tenantCancelRental(@PathVariable UUID rentalId, @RequestBody PutRequest request) {
        var presenter = new RestTenantUpdateRentalPresenter();
        var userId = authService.getAuthenticatedUserId();
        var requestModel = request.toTenantUpdateRequestModel(userId, rentalId);

        tenantUpdateRentalService.cancelRental(presenter, requestModel);
        return presenter.responseEntity() != null ?
                presenter.responseEntity()
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

/*    @DeleteMapping("/{rentalId}")
    public ResponseEntity<?> deleteRental(@PathVariable UUID rentalId) {
        Rental rental = deleteRentalService.getRentalById(rentalId);
        UUID deletedId = deleteRentalService.delete(rental);
        return ResponseEntity.ok("Rental deleted successfully: " + deletedId);
    }
*/

}
