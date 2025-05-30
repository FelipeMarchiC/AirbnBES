package br.ifsp.application.user;

import br.ifsp.application.property.repository.PropertyMapper;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.models.user.UserEntity;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        User user = User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .lastname(entity.getLastname())
                .email(entity.getEmail())
                .ownedProperties(new ArrayList<>())
                .build();

        if (entity.getOwnedProperties() != null) {
            for (PropertyEntity propertyEntity : entity.getOwnedProperties()) {
                Property property = PropertyMapper.toDomain(propertyEntity);
                user.addOwnedProperty(property);
            }
        }

        return user;
    }

    public static UserEntity toEntity(User user, String password) {
        return UserEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .password(password)
                .ownedProperties(
                        user.getOwnedProperties().stream()
                                .map(PropertyMapper::toEntity)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public static UserEntity toEntity(User user) {
        return toEntity(user, "defaultTestPassword123");
    }
}

