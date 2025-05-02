package br.ifsp.application.rental.update;

import br.ifsp.domain.models.rental.Rental;

import java.util.UUID;

public interface ITenantUpdateRentalService {
    Rental cancelRental(UUID tenantId, UUID rentalId);
}
