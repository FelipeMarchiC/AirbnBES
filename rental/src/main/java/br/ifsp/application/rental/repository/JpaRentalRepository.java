package br.ifsp.application.rental.repository;

import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface JpaRentalRepository extends JpaRepository<RentalEntity, UUID> {
    List<RentalEntity> findByPropertyEntityId(UUID propertyId);

    List<RentalEntity> findByUserEntityId(UUID tenantId);

    @Query("SELECT r FROM RentalEntity r WHERE r.propertyEntity.id = :propertyId " +
            "AND r.state = :state " +
            "AND ((r.startDate BETWEEN :startDate AND :endDate) " +
            "OR (r.endDate BETWEEN :startDate AND :endDate) " +
            "OR (r.startDate <= :startDate AND r.endDate >= :endDate)) " +
            "AND r.id != :rentalId")
    List<RentalEntity> findRentalsByOverlapAndState(
            @Param("propertyId") UUID propertyId,
            @Param("state") RentalState state,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rentalId") UUID rentalId
    );
}
