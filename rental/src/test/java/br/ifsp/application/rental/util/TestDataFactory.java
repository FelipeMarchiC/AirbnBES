package br.ifsp.application.rental.util;

import br.ifsp.application.property.repository.PropertyMapper;
import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.rental.update.owner.IOwnerUpdateRentalService;
import br.ifsp.application.rental.update.tenant.ITenantUpdateRentalService;
import br.ifsp.application.user.repository.UserMapper;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.models.user.UserEntity;
import br.ifsp.domain.shared.valueobjects.Address;
import br.ifsp.domain.shared.valueobjects.Price;
import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;

import br.ifsp.application.rental.create.ICreateRentalService;
import lombok.val;

public class TestDataFactory {
    private final Faker faker = new Faker();
    private final Clock clock;

    public final UUID rentalId = UUID.randomUUID();
    public final UUID tenantId = UUID.randomUUID();
    public final UUID ownerId = UUID.randomUUID();
    public final UUID propertyId = UUID.randomUUID();

    public TestDataFactory(Clock clock) {
        this.clock = clock;
    }

    public UserEntity generateTenantEntity(){
        return UserMapper.toEntity(generateTenant());
    }

    public UserEntity generateTenantEntity(User tenant) {
        return UserMapper.toEntity(tenant);
    }


    public UserEntity generateOwnerEntity() {
        return UserMapper.toEntity(generateOwner());
    }


    public UserEntity generateOwnerEntity(User owner) {
        return UserMapper.toEntity(owner);
    }

    public PropertyEntity generatePropertyEntity() {
        return PropertyMapper.toEntity(generateProperty());
    }

    public PropertyEntity generatePropertyEntity(Property property) {
        return PropertyMapper.toEntity(property);
    }

    public PropertyEntity generatePropertyEntity(UserEntity ownerEntity) {
        Property property = generateProperty(UserMapper.toDomain(ownerEntity));
        return PropertyMapper.toEntity(property);
    }

    public RentalEntity generateRentalEntity(
            UUID rentalId,
            UserEntity tenantEntity,
            PropertyEntity propertyEntity,
            LocalDate startDate,
            LocalDate endDate,
            RentalState state
    ) {
        Rental rental = generateRental(
                rentalId,
                UserMapper.toDomain(tenantEntity),
                PropertyMapper.toDomain(propertyEntity, clock),
                startDate,
                endDate,
                state
        );

        RentalEntity rentalEntity = RentalMapper.toEntity(rental);
        propertyEntity.getRentals().add(rentalEntity);

        return rentalEntity;
    }


    public User generateTenant() {
        return User.builder()
                .id(tenantId)
                .name(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .build();
    }

    public User generateOwner() {
        return User.builder()
                .id(ownerId)
                .name(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .build();
    }

    public Property generateProperty() {
        return Property.builder()
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
                .rentals(new ArrayList<>())
                .build();
    }

    public Property generateProperty(User owner) {
        return Property.builder()
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
                .rentals(new ArrayList<>())
                .build();
    }

    public Rental generateRental(
            UUID thisRentalId,
            User tenant,
            Property property,
            LocalDate startDate,
            LocalDate endDate,
            RentalState state
    ) {
        val rental = Rental.builder()
                .id(thisRentalId)
                .user(tenant)
                .property(property)
                .startDate(startDate)
                .endDate(endDate)
                .value(new Price(calculateRentalCost(startDate, endDate, property)))
                .state(state)
                .clock(clock)
                .build();

        property.addRental(rental);

        return rental;
    }

    public Rental generateRental(
            User tenant,
            Property property,
            LocalDate startDate,
            LocalDate endDate,
            RentalState state
    ) {
        val rental = Rental.builder()
                .id(UUID.randomUUID())
                .user(tenant)
                .property(property)
                .startDate(startDate)
                .endDate(endDate)
                .value(new Price(calculateRentalCost(startDate, endDate, property)))
                .state(state)
                .clock(clock)
                .build();

        property.addRental(rental);

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

    public Rental entityFromCreateRequest(
            ICreateRentalService.RequestModel request,
            User tenant,
            Property property
    ) {
        BigDecimal totalCost = calculateRentalCost(request.startDate(), request.endDate(), property);

        return RentalMapper.fromCreateRequestModel(rentalId, request, tenant, property, totalCost, clock);
    }

    private static BigDecimal calculateRentalCost(LocalDate start, LocalDate end, Property property) {
        long days = ChronoUnit.DAYS.between(start, end);
        return property.getDailyRate().getAmount().multiply(BigDecimal.valueOf(days));
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
