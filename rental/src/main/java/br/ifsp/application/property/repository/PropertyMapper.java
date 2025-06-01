package br.ifsp.application.property.repository;

import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.user.repository.UserMapper;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.property.PropertyEntity;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

public class PropertyMapper {

    public static Property toDomain(PropertyEntity entity, Clock clock) {
        return Property.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .dailyRate(entity.getDailyRate())
                .address(entity.getAddress())
                .owner(UserMapper.toDomainShallow(entity.getOwner()))
                .rentals(entity.getRentals().stream()
                        .map(r -> RentalMapper.toDomain(r, clock))
                        .collect(Collectors.toList()))
                .build();
    }

    public static Property toDomain(PropertyEntity entity) {
        return toDomain(entity, null);
    }

    public static Property toShallowDomain(PropertyEntity entity) {
        return Property.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .dailyRate(entity.getDailyRate())
                .address(entity.getAddress())
                .owner(UserMapper.toDomainShallow(entity.getOwner()))
                .rentals(List.of())
                .build();
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

