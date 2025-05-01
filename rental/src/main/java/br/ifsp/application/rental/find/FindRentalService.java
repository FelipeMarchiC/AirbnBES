package br.ifsp.application.rental.find;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;

import java.util.List;
import java.util.UUID;

public class FindRentalService {
    private final JpaRentalRepository jpaRentalRepository;

    public FindRentalService(JpaRentalRepository jpaRentalRepository) {
        this.jpaRentalRepository = jpaRentalRepository;
    }
    public List<Rental> getRentalHistoryByProperty(UUID propertyId) {
        return jpaRentalRepository.findByPropertyId(propertyId);
    }
    public List<Rental> getRentalHistoryByTenant(UUID tenantId) {
        return jpaRentalRepository.findByTenantId(tenantId);

    }
}
