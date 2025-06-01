package br.ifsp.infrastructure.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UuidGeneratorServiceTest {
    private UuidGeneratorService sut;

    @BeforeEach
    void setupService() {
        sut = new UuidGeneratorService();
    }

    @Test()
    @DisplayName("Should properly return generated UUID")
    void shouldProperlyReturnGeneratedUUID() {
        UUID uuid = sut.generate();
        assertThat(uuid).isNotNull();
    }
}