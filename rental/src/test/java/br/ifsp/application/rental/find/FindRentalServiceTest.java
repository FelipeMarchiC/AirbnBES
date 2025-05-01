package br.ifsp.application.rental.find;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindRentalServiceTest {

    private JpaRentalRepository jpaRentalRepository;
    private FindRentalService findRentalService;

    @BeforeEach
    void setup() {
        jpaRentalRepository = mock(JpaRentalRepository.class);
        findRentalService = new FindRentalService(jpaRentalRepository);
    }

    @Nested
    @DisplayName("Rental History Retrieval Tests")
    class RentalHistoryRetrievalTests {
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should return all rentals for a given property ID")
        @Test
        void shouldReturnAllRentalsForGivenPropertyId() {
            UUID propertyId = UUID.randomUUID();
            List<Rental> mockRentals = List.of(
                    mock(Rental.class),
                    mock(Rental.class)
            );
            when(jpaRentalRepository.findByPropertyId(propertyId)).thenReturn(mockRentals);
            List<Rental> result = findRentalService.getRentalHistory(propertyId);
            assertThat(result).isEqualTo(mockRentals);
            verify(jpaRentalRepository).findByPropertyId(propertyId);
        }
    }
}