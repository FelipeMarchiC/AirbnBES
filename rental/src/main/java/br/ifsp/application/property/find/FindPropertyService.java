package br.ifsp.application.property.find;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.domain.models.property.Property;

import java.util.List;

public class FindPropertyService {
    private final JpaPropertyRepository jpaPropertyRepository;

    public FindPropertyService(JpaPropertyRepository jpaPropertyRepository) {
        this.jpaPropertyRepository = jpaPropertyRepository;
    }

    public List<Property> findByLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("location cannot be null or blank");
        }
        return jpaPropertyRepository.findByLocation(location);
    }

    public List<Property> findByPriceRange(double min, double max) {
        if (min < 0 || max < 0) {
            throw new IllegalArgumentException("Prices must be non-negative");
        }
        if (min > max) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
        return jpaPropertyRepository.findByDailyRateBetween(min, max);
    }

    public List<Property> findAll() {
        return null;
    }
}
