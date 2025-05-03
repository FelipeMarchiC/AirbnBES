package br.ifsp.application.property;

import br.ifsp.domain.models.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JpaPropertyRepository extends JpaRepository<Property, UUID> {
    @Query("SELECT p FROM Property p WHERE p.address.city = :location")
    List<Property> findByLocation(String location);
}
