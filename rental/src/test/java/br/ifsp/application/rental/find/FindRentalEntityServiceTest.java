package br.ifsp.application.rental.find;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.repository.RentalMapper;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.domain.models.rental.Rental;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.cglib.core.Local;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindRentalEntityServiceTest {

    @Mock
    private JpaRentalRepository jpaRentalRepository;

    @InjectMocks
    private FindRentalService findRentalService;
    private FindRentalPresenter presenter;

    private IFindRentalService.FindByPropertyIdRequestModel findByPropertyIdRequestModel;
    private IFindRentalService.ResponseModel response;
    private Exception exceptionResponse;
    private Clock clock;
    private TestDataFactory factory = new TestDataFactory(clock);

    @BeforeEach
    void setup() {
        clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        jpaRentalRepository = mock(JpaRentalRepository.class);
        findRentalService = new FindRentalService(jpaRentalRepository,clock);
        presenter = new FindRentalPresenter() {
            @Override
            public void prepareSuccessView(IFindRentalService.ResponseModel response) {
                FindRentalEntityServiceTest.this.response = response;
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
    @Tag("UnitTest")
    @DisplayName("Rental History Retrieval Tests")
    class RentalEntityHistoryRetrievalTests {
        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should return all rentals for a given property ID")
        @Test
        void shouldReturnAllRentalsForGivenPropertyId() {
            UUID propertyId = UUID.randomUUID();
            RentalEntity rentalEntity = factory.generateRentalEntity(
                    UUID.randomUUID(),
                    factory.generateTenantEntity(),
                    factory.generatePropertyEntity(),
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    RentalState.CONFIRMED
            );
            RentalEntity rentalEntity1 = factory.generateRentalEntity(
                    UUID.randomUUID(),
                    factory.generateTenantEntity(),
                    factory.generatePropertyEntity(),
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    RentalState.CONFIRMED
            );
            List<RentalEntity> rentalEntities = List.of(

                    rentalEntity,
                    rentalEntity1
            );
            List<Rental> rentalList = List.of(
                    RentalMapper.toDomain(rentalEntity,clock),
                    RentalMapper.toDomain(rentalEntity1,clock)
            );

            findByPropertyIdRequestModel = new IFindRentalService.FindByPropertyIdRequestModel(propertyId);
            when(jpaRentalRepository.findByPropertyEntityId(propertyId)).thenReturn(rentalEntities);
            findRentalService.getRentalHistoryByProperty(findByPropertyIdRequestModel, presenter);
            assertThat(response).isNotNull();
            assertThat(response.rentalList()).hasSize(rentalEntities.size());
            assertThat(exceptionResponse).isNull();
            verify(jpaRentalRepository).findByPropertyEntityId(propertyId);
        }
        @Test
        @Tag("Structural")
        @DisplayName("should prepare a fail view for exceptiont")
        void shouldPrepareAFailViewForException() {
            FindRentalPresenter fakePresenter= mock(FindRentalPresenter.class);
            UUID propertyId = UUID.randomUUID();
            findByPropertyIdRequestModel = new IFindRentalService.FindByPropertyIdRequestModel(propertyId);
            RuntimeException runtimeException = new RuntimeException();
            when(jpaRentalRepository.findByPropertyEntityId(findByPropertyIdRequestModel.propertyId())).thenThrow(runtimeException);
            findRentalService.getRentalHistoryByProperty(findByPropertyIdRequestModel,fakePresenter);
            verify(jpaRentalRepository).findByPropertyEntityId(findByPropertyIdRequestModel.propertyId());
            verify(fakePresenter).prepareFailView(runtimeException);
        }
    }

    @Nested
    @Tag("UnitTest")
    @DisplayName("Tenant Rental History Retrieval Tests")
    class TenantRentalEntityHistoryRetrievalTests {
        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should return all rentals for a given tenant ID")
        @Test
        void shouldReturnAllRentalsForGivenTenantId() {
            UUID tenantId = UUID.randomUUID();
            UUID propertyId = UUID.randomUUID();
            RentalEntity rentalEntity = factory.generateRentalEntity(
                    UUID.randomUUID(),
                    factory.generateTenantEntity(),
                    factory.generatePropertyEntity(),
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    RentalState.CONFIRMED
            );
            RentalEntity rentalEntity1 = factory.generateRentalEntity(
                    UUID.randomUUID(),
                    factory.generateTenantEntity(),
                    factory.generatePropertyEntity(),
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    RentalState.CONFIRMED
            );
            List<RentalEntity> rentalEntities = List.of(
                    rentalEntity,
                    rentalEntity1
            );

            IFindRentalService.FindByTenantIdRequestModel requestModel = new IFindRentalService.FindByTenantIdRequestModel(tenantId);
            when(jpaRentalRepository.findByUserEntityId(tenantId)).thenReturn(rentalEntities);
            findRentalService.getRentalHistoryByTenant(requestModel, presenter);
            assertThat(response.rentalList()).hasSize(rentalEntities.size());
            assertThat(exceptionResponse).isNull();
            verify(jpaRentalRepository).findByUserEntityId(tenantId);
        }

        @Test
        @Tag("Structural")
        @DisplayName("Should prepare a fail view")
        void shouldPrepareAFailView(){
            UUID tenantId = UUID.randomUUID();
            IFindRentalService.FindByTenantIdRequestModel request = new IFindRentalService.FindByTenantIdRequestModel(tenantId);
            RuntimeException runtimeException = new RuntimeException();
            FindRentalPresenter fakePresenter = mock(FindRentalPresenter.class);
            when(jpaRentalRepository.findByUserEntityId(request.tenantId())).thenThrow(runtimeException);

            findRentalService.getRentalHistoryByTenant(request,fakePresenter);
            verify(fakePresenter).prepareFailView(runtimeException);


        }
    }

    @Nested
    @Tag("UnitTest")
    @DisplayName("Null Input Validation Tests")
    class NullInputValidationTests {

        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should throw exception when property ID is null")
        @Test
        void shouldThrowExceptionWhenPropertyIdIsNull() {
            findByPropertyIdRequestModel = new IFindRentalService.FindByPropertyIdRequestModel(null);

            assertThatThrownBy(() -> findRentalService.getRentalHistoryByProperty(findByPropertyIdRequestModel, presenter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("propertyId cannot be null");
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should throw exception when tenant ID is null")
        @Test
        void shouldThrowExceptionWhenTenantIdIsNull() {
            IFindRentalService.FindByTenantIdRequestModel requestModel = new IFindRentalService.FindByTenantIdRequestModel(null);

            assertThatThrownBy(() -> findRentalService.getRentalHistoryByTenant(requestModel, presenter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tenantId cannot be null");
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should throw exception when property request model is null")
        @Test
        void shouldThrowExceptionWhenRequestModelIsNull_Property() {
            assertThatThrownBy(() -> findRentalService.getRentalHistoryByProperty(null, presenter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("propertyId cannot be null");
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should throw exception when tenant request model is null")
        @Test
        void shouldThrowExceptionWhenRequestModelIsNull_Tenant() {
            assertThatThrownBy(() -> findRentalService.getRentalHistoryByTenant(null, presenter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tenantId cannot be null");
        }
    }

    @Nested
    @Tag("UnitTest")
    @DisplayName("Find All Rentals Tests")
    class FindAllRentalsTests {

        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should return all rentals when list is not empty")
        @Test
        void shouldReturnAllRentals() {
            RentalEntity rentalEntity = factory.generateRentalEntity(
                    UUID.randomUUID(),
                    factory.generateTenantEntity(),
                    factory.generatePropertyEntity(),
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    RentalState.CONFIRMED
            );
            RentalEntity rentalEntity1 = factory.generateRentalEntity(
                    UUID.randomUUID(),
                    factory.generateTenantEntity(),
                    factory.generatePropertyEntity(),
                    LocalDate.now(),
                    LocalDate.now().plusDays(10),
                    RentalState.CONFIRMED
            );
            List<RentalEntity> rentalEntities = List.of(
                    rentalEntity,
                    rentalEntity1
            );

            when(jpaRentalRepository.findAll()).thenReturn(rentalEntities);
            findRentalService.findAll(presenter);

            assertThat(response).isNotNull();
            assertThat(response.rentalList()).hasSize(rentalEntities.size());
            assertThat(exceptionResponse).isNull();
            verify(jpaRentalRepository).findAll();
        }

        @Tag("UnitTest")
        @Tag("Functional")
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
