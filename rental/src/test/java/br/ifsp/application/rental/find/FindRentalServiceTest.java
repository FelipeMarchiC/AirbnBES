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
            List<Rental> result = findRentalService.getRentalHistoryByProperty(propertyId);
            assertThat(result).isEqualTo(mockRentals);
            verify(jpaRentalRepository).findByPropertyId(propertyId);
        }
    }
    @Nested
    @DisplayName("Tenant Rental History Retrieval Tests")
    class TenantRentalHistoryRetrievalTests {
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should return all rentals for a given tenant ID")
        @Test
        void shouldReturnAllRentalsForGivenTenantId() {
            UUID tenantId = UUID.randomUUID();
            List<Rental> mockRentals = List.of(
                    mock(Rental.class),
                    mock(Rental.class)
            );
            when(jpaRentalRepository.findByUserId(tenantId)).thenReturn(mockRentals);
            List<Rental> result = findRentalService.getRentalHistoryByTenant(tenantId);
            assertThat(result).isEqualTo(mockRentals);
            verify(jpaRentalRepository).findByUserId(tenantId);
        }
    }

    @Tag("UnitTest")
    @DisplayName("Should throw exception when property ID is null")
    @Test
    void shouldThrowExceptionWhenPropertyIdIsNull_explicitEquivalenceClass() {
        assertThatThrownBy(() -> findRentalService.getRentalHistoryByProperty(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("propertyId cannot be null");
    }

    @Tag("UnitTest")
    @DisplayName("Should throw exception when tenant ID is null")
    @Test
    void shouldThrowExceptionWhenTenantIdIsNull_explicitEquivalenceClass() {
        assertThatThrownBy(() -> findRentalService.getRentalHistoryByTenant(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantId cannot be null");
    }

}