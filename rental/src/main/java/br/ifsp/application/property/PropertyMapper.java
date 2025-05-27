package br.ifsp.application.property;

import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.user.UserMapper;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PropertyMapper {

    public static Property toDomain(PropertyEntity entity) {
        User owner = new User(entity.getOwner());

        List<Rental> rentals = new ArrayList<>();
        Property property = new Property(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDailyRate(),
                entity.getAddress(),
                owner,
                rentals
        );

        if (entity.getRentals() != null) {
            for (RentalEntity rentalEntity : entity.getRentals()) {
                Rental rental = RentalMapper.toDomain(rentalEntity, property);
                rentals.add(rental);
            }
        }

        return property;
    }

    public static PropertyEntity toEntity(Property property) {
        return PropertyEntity.builder()
                .id(property.getId())
                .name(property.getName())
                .description(property.getDescription())
                .dailyRate(property.getDailyRate())
                .address(property.getAddress())
                .owner(UserMapper.toEntity(property.getOwner()))
                .rentals(
                        property.getRentals().stream()
                                .map(RentalMapper::toEntity)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public static PropertyEntity toShallowEntity(Property property) {
        return PropertyEntity.builder()
                .id(property.getId())
                .name(property.getName())
                .description(property.getDescription())
                .dailyRate(property.getDailyRate())
                .address(property.getAddress())
                .owner(UserMapper.toEntity(property.getOwner()))
                .rentals(List.of())
                .build();
    }
}

