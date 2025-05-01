package br.ifsp.application.rental.repository;

import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaRentalRepository extends JpaRepository<Rental, UUID> {
    List<Rental> findByPropertyId(UUID propertyId);
    List<Rental> findByTenantId(UUID tenantId);
}

