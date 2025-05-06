package br.ifsp.application.rental.find;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.domain.models.rental.Rental;
import org.junit.jupiter.api.*;
import org.mockito.Mock;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindRentalServiceTest {

    private JpaRentalRepository jpaRentalRepository;
    private FindRentalService findRentalService;
    @Mock
    private FindRentalPresenter presenter;
    private IFindRentalService.FindByPropertyIdRequestModel findByPropertyIdRequestModel;
    private IFindRentalService.ResponseModel response;
    private Exception exceptionResponse;

    @BeforeEach
    void setup() {
        jpaRentalRepository = mock(JpaRentalRepository.class);
        findRentalService = new FindRentalService(jpaRentalRepository);
        presenter = new FindRentalPresenter() {
            @Override
            public void prepareSuccessView(IFindRentalService.ResponseModel response) {
                FindRentalServiceTest.this.response = response;
            }

            @Override
            public void prepareFailView(Throwable exception) {
                exceptionResponse = (Exception) exception;
            }

            @Override
            public boolean isDone() {
                return false;
            }
        };
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
            response = new IFindRentalService.ResponseModel(mockRentals);
            findByPropertyIdRequestModel = new IFindRentalService.FindByPropertyIdRequestModel(propertyId);

            when(jpaRentalRepository.findByPropertyId(propertyId)).thenReturn(mockRentals);
            findRentalService.getRentalHistoryByProperty(findByPropertyIdRequestModel, presenter);
            assertThat(response.rentalList()).isEqualTo(mockRentals);
            assertThat(exceptionResponse).isNull();
            assertThat(response).isNotNull();
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
            IFindRentalService.FindByTenantIdRequestModel requestModel = new IFindRentalService.FindByTenantIdRequestModel(tenantId);
            when(jpaRentalRepository.findByUserId(tenantId)).thenReturn(mockRentals);
            findRentalService.getRentalHistoryByTenant(requestModel, presenter);
            assertThat(response.rentalList()).isEqualTo(mockRentals);
            assertThat(exceptionResponse).isNull();
            verify(jpaRentalRepository).findByUserId(tenantId);
        }
    }

    @Nested
    @DisplayName("Null Input Validation Tests")
    class NullInputValidationTests {

        @Tag("UnitTest")
        @DisplayName("Should throw exception when property ID is null")
        @Test
        void shouldThrowExceptionWhenPropertyIdIsNull() {
            findByPropertyIdRequestModel = new IFindRentalService.FindByPropertyIdRequestModel(null);

            assertThatThrownBy(() -> findRentalService.getRentalHistoryByProperty(findByPropertyIdRequestModel, presenter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("propertyId cannot be null");
        }

        @Tag("UnitTest")
        @DisplayName("Should throw exception when tenant ID is null")
        @Test
        void shouldThrowExceptionWhenTenantIdIsNull() {
            IFindRentalService.FindByTenantIdRequestModel requestModel = new IFindRentalService.FindByTenantIdRequestModel(null);

            assertThatThrownBy(() -> findRentalService.getRentalHistoryByTenant(requestModel, presenter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tenantId cannot be null");
        }

        @Tag("UnitTest")
        @DisplayName("Should throw exception when property request model is null")
        @Test
        void shouldThrowExceptionWhenRequestModelIsNull_Property() {
            assertThatThrownBy(() -> findRentalService.getRentalHistoryByProperty(null, presenter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("propertyId cannot be null");
        }

        @Tag("UnitTest")
        @DisplayName("Should throw exception when tenant request model is null")
        @Test
        void shouldThrowExceptionWhenRequestModelIsNull_Tenant() {
            assertThatThrownBy(() -> findRentalService.getRentalHistoryByTenant(null, presenter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tenantId cannot be null");
        }
    }

    @Nested
    @DisplayName("Find All Rentals Tests")
    class FindAllRentalsTests {

        @Tag("UnitTest")
        @DisplayName("Should return all rentals when list is not empty")
        @Test
        void shouldReturnAllRentals() {
            List<Rental> mockRentals = List.of(
                    mock(Rental.class),
                    mock(Rental.class)
            );

            when(jpaRentalRepository.findAll()).thenReturn(mockRentals);
            findRentalService.findAll(presenter);

            assertThat(response).isNotNull();
            assertThat(response.rentalList()).isEqualTo(mockRentals);
            assertThat(exceptionResponse).isNull();
            verify(jpaRentalRepository).findAll();
        }

        @Tag("UnitTest")
        @DisplayName("Should handle empty rental list with EntityNotFoundException")
        @Test
        void shouldHandleEmptyRentalList() {
            when(jpaRentalRepository.findAll()).thenReturn(List.of());

            findRentalService.findAll(presenter);

            assertThat(response).isNull();
            assertThat(exceptionResponse)
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("There are no rentals registered");
            verify(jpaRentalRepository).findAll();
        }
    }
}
