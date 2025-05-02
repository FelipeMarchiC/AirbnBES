package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.user.JpaUserRepository;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalState;

import java.util.UUID;

public class TenantUpdateRentalService implements ITenantUpdateRentalService {
    private final JpaRentalRepository rentalRepository;
    private final JpaUserRepository userRepository;

    public TenantUpdateRentalService(
            JpaRentalRepository rentalRepository,
            JpaUserRepository userRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Rental cancelRental(UUID tenantId, UUID rentalId) {

        userRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

        if (rental.getState() != RentalState.CONFIRMED) {
            throw new IllegalArgumentException("Rental is not in a valid state to be cancelled");
        }

        rental.setState(RentalState.CANCELLED);

        return rentalRepository.save(rental);
    }
}
