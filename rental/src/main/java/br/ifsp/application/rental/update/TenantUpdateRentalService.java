package br.ifsp.application.rental.update;

import br.ifsp.domain.models.rental.Rental;

import java.time.LocalDate;
import java.util.UUID;

public class TenantUpdateRentalService implements ITenantUpdateRentalService {
    @Override
    public Rental cancelRental(UUID tenantId, UUID rentalId) {
        return null;
    }
}
