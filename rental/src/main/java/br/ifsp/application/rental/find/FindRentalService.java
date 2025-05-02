package br.ifsp.application.rental.find;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FindRentalService {
    private final JpaRentalRepository jpaRentalRepository;

    public FindRentalService(JpaRentalRepository jpaRentalRepository) {
        this.jpaRentalRepository = jpaRentalRepository;
    }

    public List<Rental> getRentalHistoryByProperty(UUID propertyId) {
        if (propertyId == null)
            throw new IllegalArgumentException("propertyId cannot be null");
        return jpaRentalRepository.findByPropertyId(propertyId);
    }

    public List<Rental> getRentalHistoryByTenant(UUID tenantId) {
        if (tenantId == null)
            throw new IllegalArgumentException("tenantId cannot be null");
        return jpaRentalRepository.findByUserId(tenantId);
    }
}
