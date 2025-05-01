package br.ifsp.application.property;

import br.ifsp.domain.models.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPropertyRepository extends JpaRepository<Property, UUID> {
}
