package br.ifsp.application.rental.update;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

public class TenantUpdateRentalService implements ITenantUpdateRentalService {
    private final JpaRentalRepository rentalRepository;

    public TenantUpdateRentalService(
            JpaRentalRepository rentalRepository
    ) {
        this.rentalRepository = rentalRepository;
    }

    @Override
    public Rental cancelRental(UUID tenantId, UUID rentalId) {

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        rental.setState(RentalState.CANCELLED);

        return rentalRepository.save(rental);
    }
}
