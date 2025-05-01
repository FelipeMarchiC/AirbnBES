package br.ifsp.application.rental.repository;

import br.ifsp.domain.models.rental.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JpaRentalRepository extends JpaRepository<Rental, UUID> {
    List<Rental> findByPropertyId(UUID propertyId);
    List<Rental> findByTenantId(UUID tenantId);
}

