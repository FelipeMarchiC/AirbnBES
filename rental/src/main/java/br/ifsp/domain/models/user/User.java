package br.ifsp.domain.models.user;

import lombok.Getter;

import java.util.UUID;

public class User {

    @Getter
    private final UUID id;

    @Getter
    private final String name;

    @Getter
    private final String lastname;

    @Getter
    private final String email;

    @Getter
    private final Role role;

    public User(UserEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.lastname = entity.getLastname();
        this.email = entity.getEmail();
        this.role = entity.getRole();
    }

    public User(UUID id, String name, String lastname, String email, Role role) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
    }

    public String getFullName() {
        return name + " " + lastname;
    }
}

