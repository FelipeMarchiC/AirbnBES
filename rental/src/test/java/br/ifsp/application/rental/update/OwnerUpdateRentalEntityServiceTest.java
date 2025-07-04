package br.ifsp.application.rental.update;

import br.ifsp.application.rental.repository.JpaRentalRepository;
import br.ifsp.application.rental.update.owner.OwnerUpdateRentalPresenter;
import br.ifsp.application.rental.update.owner.OwnerUpdateRentalService;
import br.ifsp.application.rental.update.owner.IOwnerUpdateRentalService.RequestModel;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.exceptions.ImmutablePastEntityException;
import br.ifsp.domain.models.property.PropertyEntity;
import br.ifsp.domain.models.rental.RentalEntity;
import br.ifsp.domain.models.rental.RentalState;
import br.ifsp.domain.models.user.UserEntity;
import org.codehaus.plexus.util.cli.Arg;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class OwnerUpdateRentalEntityServiceTest {

    private final String string = "";
    @Mock
    private JpaRentalRepository rentalRepositoryMock;

    @Mock
    private OwnerUpdateRentalPresenter presenter;

    @InjectMocks
    private OwnerUpdateRentalService sut;

    private AutoCloseable closeable;

    private TestDataFactory testDataFactory;
    private UserEntity tenantEntity;
    private UserEntity ownerEntity;
    private PropertyEntity propertyEntity;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        testDataFactory = new TestDataFactory(fixedClock);
        tenantEntity = testDataFactory.generateTenantEntity();
        ownerEntity = testDataFactory.generateOwnerEntity();
        propertyEntity = testDataFactory.generatePropertyEntity(ownerEntity);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("Should throw security exception when non owner tries to confirm rental")
    void shouldThrowSecurityExceptionWhenNonOwnerTriesToConfirmRental() {
        RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                UUID.randomUUID(),
                tenantEntity,
                propertyEntity,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                RentalState.PENDING
        );

        when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

        UserEntity nonOwner = testDataFactory.generateTenantEntity();

        RequestModel request = new RequestModel(nonOwner.getId(), rentalEntity.getId());

        sut.confirmRental(presenter, request);

        verify(presenter).prepareFailView(any(SecurityException.class));
    }


    @Nested
    @DisplayName("Rental Denial Tests")
    class DenyRentalEntityServiceTest {
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        void shouldSetPendingRentalAsDeniedIfOwnerDenies() {
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    RentalState.PENDING
            );
            ArgumentCaptor<RentalEntity> captor = ArgumentCaptor.forClass(RentalEntity.class);
            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(rentalRepositoryMock.save(any())).thenAnswer(inv->inv.getArgument(0));
            RequestModel request = new RequestModel(ownerEntity.getId(), rentalEntity.getId());
            sut.denyRental(presenter, request);
            verify(rentalRepositoryMock).save(captor.capture());
            RentalEntity entity= captor.getValue();
            assertThat(entity.getState()).isEqualTo(RentalState.DENIED);

            verify(presenter).prepareSuccessView(any());
        }

        @Test
        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("Should throw security exception when non owner tries to deny rental")
        void shouldThrowSecurityExceptionWhenNonOwnerTriesToDenyRental() {
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    RentalState.PENDING
            );

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            UserEntity nonOwner = testDataFactory.generateTenantEntity();

            RequestModel request = new RequestModel(nonOwner.getId(), rentalEntity.getId());

            sut.denyRental(presenter, request);

            verify(presenter).prepareFailView(any(SecurityException.class));
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"CONFIRMED", "EXPIRED", "DENIED"})
        void shouldNotPermitDenialIfRentalNotPendingOrRestrained(RentalState state) {
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    state
            );

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            RequestModel request = new RequestModel(ownerEntity.getId(), rentalEntity.getId());
            sut.denyRental(presenter, request);

            verify(presenter).prepareFailView(any(UnsupportedOperationException.class));
        }
    }

    @Nested
    class CancelRentalEntityTests {

        @BeforeEach
        void setupClock() {
            Clock fixedClock = Clock.fixed(LocalDate.of(2025, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
            sut = new OwnerUpdateRentalService(rentalRepositoryMock, fixedClock);
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"PENDING", "DENIED", "EXPIRED", "RESTRAINED", "CANCELLED"})
        void shouldNotCancelIfRentalIsNotConfirmed(RentalState state) {
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.of(2025, 1, 2),
                    LocalDate.of(2025, 1, 10),
                    state
            );

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            sut.cancelRental(presenter, new RequestModel(ownerEntity.getId(), rentalEntity.getId()), null);

            verify(presenter).prepareFailView(any(IllegalArgumentException.class));
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("Should not cancel a rental from a different Owner")
        void shouldNotCancelARentalFromADifferentOwners() {
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    RentalState.PENDING
            );

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            UserEntity nonOwner = testDataFactory.generateTenantEntity();  // Diferente do proprietário

            RequestModel request = new RequestModel(nonOwner.getId(), rentalEntity.getId());

            sut.cancelRental(presenter, request, null);

            verify(presenter).prepareFailView(any(SecurityException.class));
        }

        @Tag("TDD")
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        void shouldNotAllowCancelAfterStartDate() {
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.of(2024, 12, 30), // startDate (no passado)
                    LocalDate.of(2025, 10, 10),
                    RentalState.CONFIRMED
            );

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            sut.cancelRental(presenter, new RequestModel(ownerEntity.getId(), rentalEntity.getId()), LocalDate.of(2024, 12, 30).minusDays(1));

            ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
            verify(presenter).prepareFailView(captor.capture());

            Exception exception = captor.getValue();
            assertThat(exception).isInstanceOf(ImmutablePastEntityException.class);
            assertThat(exception.getMessage()).isEqualTo("This operation must be current or future dates.");
        }


        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("Should Cancel Rental Successfully")
        void shouldCancelRentalSuccessfully() {
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.of(2025, 1, 2),
                    LocalDate.of(2025, 1, 10),
                    RentalState.CONFIRMED
            );

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.RESTRAINED), any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(rentalRepositoryMock.save(any(RentalEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<RentalEntity> captor = ArgumentCaptor.forClass(RentalEntity.class);


            sut.cancelRental(presenter, new RequestModel(ownerEntity.getId(), rentalEntity.getId()), LocalDate.of(2025, 1, 2).minusDays(2));

            verify(rentalRepositoryMock).save(captor.capture());
            RentalEntity savedRentalEntity = captor.getValue();
            assertThat(savedRentalEntity.getState()).isEqualTo(RentalState.CANCELLED);
            verify(presenter).prepareSuccessView(any());
        }
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        void shouldSetRestrainedConflictsToPending() {
            // Arrange
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.of(2025, 1, 2),
                    LocalDate.of(2025, 1, 10),
                    RentalState.CONFIRMED
            );

            RentalEntity r1 = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    rentalEntity.getStartDate(),
                    rentalEntity.getEndDate(),
                    RentalState.RESTRAINED
            );

            RentalEntity r2 = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    rentalEntity.getStartDate(),
                    rentalEntity.getEndDate(),
                    RentalState.RESTRAINED
            );

            // ArgumentCaptor para capturar a lista passada ao saveAll
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<RentalEntity>> captor = ArgumentCaptor.forClass(List.class);

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.RESTRAINED), any(), any(), any()))
                    .thenReturn(List.of(r1, r2));

            when(rentalRepositoryMock.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            sut.cancelRental(presenter, new RequestModel(ownerEntity.getId(), rentalEntity.getId()), null);

            // Assert
            verify(rentalRepositoryMock).saveAll(captor.capture());
            List<RentalEntity> capturedRentals = captor.getValue();

            assertThat(capturedRentals).hasSize(2);
            assertThat(capturedRentals).allSatisfy(rental ->
                    assertThat(rental.getState()).isEqualTo(RentalState.PENDING)
            );
        }

        @Test
        @DisplayName("should prepare a fail view for Canceling a not found Rental")
        void shouldPrepareAFailViewForCancelingANotFoundRental(){
            UUID ownerId = UUID.randomUUID();
            UUID rentalId = UUID.randomUUID();
            RequestModel requestModel = new RequestModel(ownerId, rentalId);
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.empty());
            sut.cancelRental(presenter,requestModel,LocalDate.now());
            verify(presenter).prepareFailView(any(EntityNotFoundException.class));
        }

    }

    @Nested
    class ConfirmRentalEntityTests {

        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("should confirm pending rental without conflict")
        void shouldConfirmPendingRentalWithoutConflict() {
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.of(2025, 2, 1),
                    LocalDate.of(2025, 10, 10),
                    RentalState.PENDING
            );
            RentalEntity conflict = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.of(2025, 2, 5),
                    LocalDate.of(2025, 10, 10),
                    RentalState.PENDING
            );

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.CONFIRMED), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.PENDING), any(), any(), any()))
                    .thenReturn(List.of(conflict));

            when(rentalRepositoryMock.save(any(RentalEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            // Capture todas as chamadas de save()
            ArgumentCaptor<RentalEntity> captor = ArgumentCaptor.forClass(RentalEntity.class);

            // Executa
            sut.confirmRental(presenter, new RequestModel(ownerEntity.getId(), rentalEntity.getId()));

            // Verifica: deve ter havido dois saves
            verify(rentalRepositoryMock, times(2)).save(captor.capture());
            List<RentalEntity> savedEntities = captor.getAllValues();

            // Checa os estados
            RentalEntity confirmed = savedEntities.stream()
                    .filter(e -> e.getId().equals(rentalEntity.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Confirmed rental not saved"));

            RentalEntity restrained = savedEntities.stream()
                    .filter(e -> e.getId().equals(conflict.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Restrained rental not saved"));

            assertThat(confirmed.getState()).isEqualTo(RentalState.CONFIRMED);
            assertThat(restrained.getState()).isEqualTo(RentalState.RESTRAINED);

            // Checa presenter
            verify(presenter).prepareSuccessView(any());
        }


        @Tag("UnitTest")
        @Tag("Functional")
        @ParameterizedTest
        @EnumSource(value = RentalState.class, names = {"CONFIRMED", "EXPIRED", "DENIED", "RESTRAINED"})
        void shouldNotConfirmRentalIfNotPending(RentalState state) {
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.of(1801, 2, 1),
                    LocalDate.of(1801, 2, 10),
                    state
            );

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));

            sut.confirmRental(presenter, new RequestModel(ownerEntity.getId(), rentalEntity.getId()));

            verify(presenter).prepareFailView(any(UnsupportedOperationException.class));
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("should throw IllegalState exception when conflicting rental exists")
        void shouldThrowIllegalStateExceptionWhenConflictingRentalExists() {
            RentalEntity rentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    RentalState.PENDING
            );

            RentalEntity conflictingRentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    RentalState.CONFIRMED
            );

            when(rentalRepositoryMock.findById(rentalEntity.getId())).thenReturn(Optional.of(rentalEntity));
            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.CONFIRMED), any(), any(), any()))
                    .thenReturn(List.of(conflictingRentalEntity));

            RequestModel request = new RequestModel(ownerEntity.getId(), rentalEntity.getId());
            sut.confirmRental(presenter, request);

            verify(presenter).prepareFailView(any(IllegalStateException.class));
        }

        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        @DisplayName("Should throw entity not found exception when rental not found")
        void shouldThrowEntityNotFoundExceptionWhenRentalNotFound() {
            when(rentalRepositoryMock.findById(any())).thenReturn(Optional.empty());

            RequestModel request = new RequestModel(ownerEntity.getId(), UUID.randomUUID());
            sut.denyRental(presenter, request);

            verify(presenter).prepareFailView(any(EntityNotFoundException.class));
        }
        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("Should Prepare a fail view for Entity not found")
        void shouldPrepareAFailViewForRentalEntityNotFound(){
            UUID ownerId = UUID.randomUUID();
            UUID rentalId = UUID.randomUUID();
            RequestModel requestModel = new RequestModel(ownerId, rentalId);
            when(rentalRepositoryMock.findById(rentalId)).thenReturn(Optional.empty());
            sut.confirmRental(presenter,requestModel);
            verify(presenter).prepareFailView(any(EntityNotFoundException.class));
        }

    }

    @Nested
    class UpdateRentalEntityTests {
        @Tag("UnitTest")
        @Tag("Functional")
        @Test
        void shouldRestrainConflictingPendingRentals() {
            RentalEntity confirmedRentalEntity = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.of(1801, 2, 1),
                    LocalDate.of(1801, 2, 10),
                    RentalState.CONFIRMED
            );

            RentalEntity conflict = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.of(2025, 2, 5),
                    LocalDate.now().plusDays(100),
                    RentalState.PENDING
            );
            RentalEntity conflict2 = testDataFactory.generateRentalEntity(
                    UUID.randomUUID(),
                    tenantEntity,
                    propertyEntity,
                    LocalDate.of(1801, 2, 5),
                    LocalDate.of(1801, 2, 8),
                    RentalState.PENDING
            );


            when(rentalRepositoryMock.findRentalsByOverlapAndState(any(), eq(RentalState.PENDING), any(), any(), any()))
                    .thenReturn(List.of(conflict,conflict2));
            ArgumentCaptor<RentalEntity> captor =ArgumentCaptor.forClass(RentalEntity.class);
            when(rentalRepositoryMock.save(any(RentalEntity.class))).thenAnswer(inv->inv.getArgument(0));

            sut.restrainPendingRentalsInConflict(confirmedRentalEntity);
            verify(rentalRepositoryMock,times(2)).save(captor.capture());
            captor.getAllValues().forEach(rentalEntity -> assertThat(rentalEntity.getState()).isNotEqualTo(RentalState.PENDING));
        }
    }




}
