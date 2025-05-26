package br.ifsp.application.rental.util;

import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.rental.update.owner.IOwnerUpdateRentalService;
import br.ifsp.application.rental.update.tenant.ITenantUpdateRentalService;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.Role;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;

import br.ifsp.application.rental.create.ICreateRentalService;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor
public class TestDataFactory {
    private final Faker faker = new Faker();

    public final UUID rentalId = UUID.randomUUID();
    public final UUID tenantId = UUID.randomUUID();
    public final UUID ownerId = UUID.randomUUID();
    public final UUID propertyId = UUID.randomUUID();

    public User generateTenant() {
        return User.builder()
                .id(tenantId)
                .name(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password())
                .role(Role.USER)
                .ownedProperties(new ArrayList<>())
                .build();
    }

    public User generateTenant(UUID thisTenantId) {
        return User.builder()
                .id(thisTenantId)
                .name(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password())
                .role(Role.USER)
                .ownedProperties(new ArrayList<>())
                .build();
    }

    public User generateOwner() {
        return User.builder()
                .id(ownerId)
                .name(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password())
                .role(Role.USER)
                .ownedProperties(new ArrayList<>())
                .build();
    }

    public PropertyEntity generateProperty() {
        return PropertyEntity.builder()
                .id(propertyId)
                .name(faker.address().streetName())
                .description(faker.lorem().sentence())
                .dailyRate(new Price(BigDecimal.valueOf(faker.number().randomDouble(2, 100, 1000))))
                .address(Address.builder()
                        .number(faker.address().buildingNumber())
                        .street(faker.address().streetAddress())
                        .city(faker.address().city())
                        .state(faker.address().state())
                        .postalCode(faker.address().zipCode())
                        .build())
                .owner(generateOwner())
                .rentalEntities(new ArrayList<>())
                .build();
    }

    public PropertyEntity generateProperty(UUID thisPropertyId) {
        return PropertyEntity.builder()
                .id(thisPropertyId)
                .name(faker.address().streetName())
                .description(faker.lorem().sentence())
                .dailyRate(new Price(BigDecimal.valueOf(faker.number().randomDouble(2, 100, 1000))))
                .address(Address.builder()
                        .number(faker.address().buildingNumber())
                        .street(faker.address().streetAddress())
                        .city(faker.address().city())
                        .state(faker.address().state())
                        .postalCode(faker.address().zipCode())
                        .build())
                .owner(generateOwner())
                .rentalEntities(new ArrayList<>())
                .build();
    }

    public PropertyEntity generateProperty(User owner) {
        return PropertyEntity.builder()
                .id(propertyId)
                .name(faker.address().streetName())
                .description(faker.lorem().sentence())
                .dailyRate(new Price(BigDecimal.valueOf(faker.number().randomDouble(2, 100, 1000))))
                .address(Address.builder()
                        .number(faker.address().buildingNumber())
                        .street(faker.address().streetAddress())
                        .city(faker.address().city())
                        .state(faker.address().state())
                        .postalCode(faker.address().zipCode())
                        .build())
                .owner(owner)
                .rentalEntities(new ArrayList<>())
                .build();
    }

    public RentalEntity generateRental() {
        return RentalEntity.builder()
                .id(rentalId)
                .user(generateTenant())
                .propertyEntity(generateProperty())
                .startDate(LocalDate.parse("2025-01-01"))
                .endDate(LocalDate.parse("2025-01-01").plusDays(7))
                .value(new Price(BigDecimal.valueOf(1500.00)))
                .state(RentalState.CONFIRMED)
                .build();
    }

    public RentalEntity generateRental(UUID thisRentalId) {
        return RentalEntity.builder()
                .id(thisRentalId)
                .user(generateTenant())
                .propertyEntity(generateProperty())
                .startDate(LocalDate.parse("2025-01-01"))
                .endDate(LocalDate.parse("2025-01-01").plusDays(7))
                .value(new Price(BigDecimal.valueOf(1500.00)))
                .state(RentalState.CONFIRMED)
                .build();
    }

    public RentalEntity generateRental(
            UUID thisRentalId,
            User tenant,
            PropertyEntity propertyEntity,
            LocalDate startDate,
            LocalDate endDate,
            RentalState state
    ) {
        val rental = RentalEntity.builder()
                .id(thisRentalId)
                .user(tenant)
                .propertyEntity(propertyEntity)
                .startDate(startDate)
                .endDate(endDate)
                .value(new Price(calculateRentalCost(startDate, endDate, propertyEntity)))
                .state(state)
                .build();

        propertyEntity.addRental(rental);

        return rental;
    }

    public RentalEntity generateRental(
            User tenant,
            PropertyEntity propertyEntity,
            LocalDate startDate,
            LocalDate endDate,
            RentalState state
    ) {
        val rental = RentalEntity.builder()
                .id(UUID.randomUUID())
                .user(tenant)
                .propertyEntity(propertyEntity)
                .startDate(startDate)
                .endDate(endDate)
                .value(new Price(calculateRentalCost(startDate, endDate, propertyEntity)))
                .state(state)
                .build();

        propertyEntity.addRental(rental);

        return rental;
    }

    public ICreateRentalService.RequestModel createRequestModel() {
        return new ICreateRentalService.RequestModel(
                tenantId,
                propertyId,
                LocalDate.parse("2025-01-01"),
                LocalDate.parse("2025-01-01").plusDays(7)
        );
    }

    public ICreateRentalService.RequestModel createRequestModel(LocalDate startDate, LocalDate endDate) {
        return new ICreateRentalService.RequestModel(
                tenantId,
                propertyId,
                startDate,
                endDate
        );
    }

    public ICreateRentalService.ResponseModel createResponseModel() {
        return new ICreateRentalService.ResponseModel(rentalId, tenantId);
    }

    public RentalEntity entityFromCreateRequest(
            ICreateRentalService.RequestModel request,
            User tenant,
            PropertyEntity propertyEntity
    ) {
        BigDecimal totalCost = calculateRentalCost(request.startDate(), request.endDate(), propertyEntity);

        return RentalMapper.fromCreateRequestModel(rentalId, request, tenant, propertyEntity, totalCost);
    }

    private static BigDecimal calculateRentalCost(LocalDate start, LocalDate end, PropertyEntity propertyEntity) {
        long days = ChronoUnit.DAYS.between(start, end);
        return propertyEntity.getDailyRate().getAmount().multiply(BigDecimal.valueOf(days));
    }

    public ITenantUpdateRentalService.RequestModel tenantUpdateRequestModel() {
        return new ITenantUpdateRentalService.RequestModel(tenantId, rentalId);
    }

    public ITenantUpdateRentalService.ResponseModel tenantUpdateResponseModel() {
        return new ITenantUpdateRentalService.ResponseModel(rentalId, tenantId);
    }

    public IOwnerUpdateRentalService.RequestModel ownerUpdateRequestModel(){
        return new IOwnerUpdateRentalService.RequestModel(ownerId,rentalId);
    }
    public IOwnerUpdateRentalService.ResponseModel ownerUpdateResponseModel(){
        return new IOwnerUpdateRentalService.ResponseModel(ownerId,rentalId);
    }
}
