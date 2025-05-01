package br.ifsp.application.rental.create;

import br.ifsp.domain.models.rental.Rental;

import java.time.LocalDate;
import java.util.UUID;

public class CreateRentalService implements ICreateRentalService {

    @Override
    public Rental registerRental(UUID userId, UUID propertyId, LocalDate startDate, LocalDate endDate) {
        return null;
    }
}
