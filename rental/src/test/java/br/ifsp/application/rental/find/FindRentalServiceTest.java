package br.ifsp.application.rental.find;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.domain.models.rental.Rental;
import lombok.val;
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
                exceptionResponse =(Exception) exception;

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
            findRentalService.getRentalHistoryByProperty(findByPropertyIdRequestModel,presenter);
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
            IFindRentalService.FindByTenantIdRequestModel requestModel= new IFindRentalService.FindByTenantIdRequestModel(tenantId);
            when(jpaRentalRepository.findByUserId(tenantId)).thenReturn(mockRentals);
            findRentalService.getRentalHistoryByTenant(requestModel,presenter);
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
            assertThatThrownBy(() -> findRentalService.getRentalHistoryByProperty(findByPropertyIdRequestModel,presenter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("propertyId cannot be null");
        }

        @Tag("UnitTest")
        @DisplayName("Should throw exception when tenant ID is null")
        @Test
        void shouldThrowExceptionWhenTenantIdIsNull() {
            assertThatThrownBy(() -> findRentalService.getRentalHistoryByTenant(null,presenter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tenantId cannot be null");
        }
    }



    @Nested
    @DisplayName("Rental FindAll Tests")
    class RentalFindAllTests {
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should return all rentals from repository")
        @Test
        void shouldReturnAllRentals() {
            List<Rental> mockRentals = List.of(
                    mock(Rental.class),
                    mock(Rental.class)
            );
            when(jpaRentalRepository.findAll()).thenReturn(mockRentals);
            List<Rental> result = findRentalService.findAll();
            assertThat(result).isEqualTo(mockRentals);
            verify(jpaRentalRepository).findAll();
        }
    }

}