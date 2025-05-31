package br.ifsp.application.property.repository;

import br.ifsp.domain.models.property.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface JpaPropertyRepository extends JpaRepository<PropertyEntity, UUID> {
    @Query("SELECT p FROM PropertyEntity p WHERE p.address.city = :location")
    List<PropertyEntity> findByLocation(String location);
    @Query("SELECT p FROM PropertyEntity p WHERE p.dailyRate.amount BETWEEN :min AND :max")
    List<PropertyEntity> findByDailyRateBetween(@Param("min") double min, @Param("max") double max);
    @Query("SELECT p FROM PropertyEntity p WHERE p.id NOT IN (SELECT r.propertyEntity.id FROM RentalEntity r WHERE r.state = :state AND ((r.startDate BETWEEN :startDate AND :endDate) OR (r.endDate BETWEEN :startDate AND :endDate) OR (r.startDate <= :startDate AND r.endDate >= :endDate)))")
    List<PropertyEntity> findAvailablePropertiesByPeriod(LocalDate startDate, LocalDate endDate);
}
