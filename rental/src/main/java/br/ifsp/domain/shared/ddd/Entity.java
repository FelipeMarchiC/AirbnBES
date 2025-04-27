package br.ifsp.domain.shared.ddd;

import lombok.Getter;

import java.util.Objects;

@Getter
public abstract class Entity<T> {
    private final Identifier<T> id;

    public Entity(Identifier<T> id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Entity<?> entity = (Entity<?>) other;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}