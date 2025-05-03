package br.ifsp.application.rental.find;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.domain.models.property.Property;

import java.util.List;

public class FindPropertyService {
    public FindPropertyService(JpaPropertyRepository jpaPropertyRepository) {
    }

    public List<Property> findByLocation(String location) {
        return null;
    }
}
