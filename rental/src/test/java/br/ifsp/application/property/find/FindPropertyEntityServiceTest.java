package br.ifsp.application.property.find;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.property.repository.PropertyMapper;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.property.PropertyEntity;
import org.junit.jupiter.api.*;
import org.mockito.Mock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindPropertyEntityServiceTest {

    @Mock
    private JpaPropertyRepository jpaPropertyRepository;

    @Mock
    private TestDataFactory factory;

    private FindPropertyService findPropertyService;

    private IFindPropertyService.PropertyListResponseModel capturedResponse;
    private IFindPropertyService.PropertyResponseModel capturedPropertyResponse;
    private Exception capturedException;

    private FindPropertyPresenter presenter;
    private FindPropertyByIdPresenter findByIdPresenter;

    @BeforeEach
    void setup() {
        jpaPropertyRepository = mock(JpaPropertyRepository.class);

        presenter = new FindPropertyPresenter() {
            @Override
            public void prepareSuccessView(IFindPropertyService.PropertyListResponseModel responseModel) {
                capturedResponse = responseModel;
            }

            @Override
            public void prepareFailView(Throwable throwable) {
                capturedException = (Exception) throwable;
            }

            @Override
            public boolean isDone() {
                return false;
            }
        };

        findByIdPresenter = new FindPropertyByIdPresenter() {
            @Override
            public void prepareSuccessView(IFindPropertyService.PropertyResponseModel responseModel) {
                capturedPropertyResponse = responseModel;
            }

            @Override
            public void prepareFailView(Throwable throwable) {
                capturedException = (Exception) throwable;
            }

            @Override
            public boolean isDone() {
                return false;
            }
        };

        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.systemDefault());
        factory = new TestDataFactory(fixedClock);
        findPropertyService = new FindPropertyService(jpaPropertyRepository);
        capturedResponse = null;
        capturedPropertyResponse = null;
        capturedException = null;
    }

    @Nested
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Structural Tests")
    class StructuralTests {
        @Test
        @DisplayName("Should ensure constructor injects JpaPropertyRepository")
        void shouldEnsureConstructorInjectsJpaPropertyRepository() {
            assertThat(findPropertyService).isNotNull();
        }
    }

    @Nested
    @DisplayName("Property search by ID tests")
    class PropertyEntitySearchById {
        @Test
        @DisplayName("Should return property when found by ID")
        void shouldReturnPropertyWhenFoundById() {
            UUID propertyId = UUID.randomUUID();
            PropertyEntity propertyEntity = factory.generatePropertyEntity(propertyId);
            Property expectedProperty = PropertyMapper.toDomain(propertyEntity);

            when(jpaPropertyRepository.findById(propertyId)).thenReturn(Optional.of(propertyEntity));

            var request = new IFindPropertyService.FindByIdRequestModel(propertyId);
            findPropertyService.findById(findByIdPresenter, request);

            assertThat(capturedException).isNull();
            assertThat(capturedPropertyResponse).isNotNull();
            assertThat(capturedPropertyResponse.property()).isEqualTo(expectedProperty);
        }
    }

    @Nested
    @DisplayName("Property search by period tests")
    class PropertyEntitySearchByPeriod {

        @Test
        @DisplayName("Should return available properties in period")
        void shouldReturnAvailablePropertiesInPeriod() {
            PropertyEntity propertyEntity = factory.generatePropertyEntity();
            Property expected = PropertyMapper.toDomain(propertyEntity);

            LocalDate startDate = LocalDate.of(2025, 5, 4);
            LocalDate endDate = startDate.plusDays(7);

            when(jpaPropertyRepository.findAvailablePropertiesByPeriod(startDate, endDate)).thenReturn(List.of(propertyEntity));

            var request = new IFindPropertyService.PeriodRequestModel(startDate, endDate);
            findPropertyService.findByPeriod(presenter, request);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).containsExactly(expected);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when no property is found")
        void shouldThrowEntityNotFoundWhenEmptyList() {
            LocalDate startDate = LocalDate.of(2025, 5, 4);
            LocalDate endDate = startDate.plusDays(7);

            when(jpaPropertyRepository.findAvailablePropertiesByPeriod(startDate, endDate)).thenReturn(List.of());

            var request = new IFindPropertyService.PeriodRequestModel(startDate, endDate);
            findPropertyService.findByPeriod(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Property Search By Location Tests")
    class PropertyEntitySearchByLocationTests {
        @Test
        @DisplayName("Should return all properties for a given location")
        void shouldReturnAllPropertiesForGivenLocation() {
            String location = "São Paulo";
            PropertyEntity entity1 = factory.generatePropertyEntity();
            PropertyEntity entity2 = factory.generatePropertyEntity();
            List<PropertyEntity> entityList = List.of(entity1, entity2);
            List<Property> expected = entityList.stream().map(PropertyMapper::toDomain).toList();

            when(jpaPropertyRepository.findByLocation(location)).thenReturn(entityList);

            var request = new IFindPropertyService.LocationRequestModel(location);
            findPropertyService.findByLocation(presenter, request);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).containsExactlyElementsOf(expected);
        }

        @Test
        @DisplayName("Should throw exception when location is null")
        void shouldThrowExceptionWhenLocationIsNull() {
            var request = new IFindPropertyService.LocationRequestModel(null);
            findPropertyService.findByLocation(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Location cannot be null or blank");
        }

        @Test
        @DisplayName("Should throw exception when location is blank")
        void shouldThrowExceptionWhenLocationIsBlank() {
            var request = new IFindPropertyService.LocationRequestModel("   ");
            findPropertyService.findByLocation(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Location cannot be null or blank");
        }
    }

    @Nested
    @DisplayName("Property Search By Price Range Tests")
    class PropertyEntitySearchByPriceRangeTests {

        @Test
        @DisplayName("Should return properties within given price range")
        void shouldReturnPropertiesWithinGivenPriceRange() {
            double min = 100.0;
            double max = 300.0;

            PropertyEntity e1 = factory.generatePropertyEntity();
            PropertyEntity e2 = factory.generatePropertyEntity();
            List<PropertyEntity> entityList = List.of(e1, e2);
            List<Property> expected = entityList.stream().map(PropertyMapper::toDomain).toList();

            when(jpaPropertyRepository.findByDailyRateBetween(min, max)).thenReturn(entityList);

            var request = new IFindPropertyService.PriceRangeRequestModel(min, max);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).containsExactlyElementsOf(expected);
        }

        @Test
        @DisplayName("Should throw exception if min > max")
        void shouldThrowExceptionIfMinGreaterThanMax() {
            var request = new IFindPropertyService.PriceRangeRequestModel(500.0, 100.0);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Minimum price cannot be greater than maximum price");
        }

        @Test
        @DisplayName("Should throw exception if min < 0")
        void shouldThrowExceptionIfMinIsNegative() {
            var request = new IFindPropertyService.PriceRangeRequestModel(-50.0, 200.0);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Prices must be non-negative");
        }

        @Test
        @DisplayName("Should throw exception if max < 0")
        void shouldThrowExceptionIfMaxIsNegative() {
            var request = new IFindPropertyService.PriceRangeRequestModel(50.0, -200.0);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Prices must be non-negative");
        }

    @Nested
    @DisplayName("Property FindAll Tests")
    class PropertyEntityFindAllTests {

        @Test
        @DisplayName("Should return all properties from repository")
        void shouldReturnAllProperties() {
            PropertyEntity e1 = factory.generatePropertyEntity();
            PropertyEntity e2 = factory.generatePropertyEntity();
            List<PropertyEntity> entityList = List.of(e1, e2);
            List<Property> expected = entityList.stream().map(PropertyMapper::toDomain).toList();

            when(jpaPropertyRepository.findAll()).thenReturn(entityList);
            findPropertyService.findAll(presenter);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).containsExactlyElementsOf(expected);
        }

        @Test
        @DisplayName("Should handle exception from repository in findAll")
        void shouldHandleExceptionInFindAll() {
            when(jpaPropertyRepository.findAll()).thenThrow(new RuntimeException("Database error"));
            findPropertyService.findAll(presenter);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");
        }
    }
}
}