package br.ifsp.application.rental.create;

import br.ifsp.domain.models.rental.Rental;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateRentalServiceTest {
    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should create rental")
    void shouldCreateRental() {
        CreateRentalService service = new CreateRentalService();
        Rental rental = service.registerRental();
        assertThat(rental).isNotNull();
    }
}
