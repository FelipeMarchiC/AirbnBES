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

        @Test
        @DisplayName("Should throw EntityNotFoundException when property not found by ID")
        void shouldThrowEntityNotFoundExceptionWhenPropertyNotFoundById() {
            UUID propertyId = UUID.randomUUID();

            when(jpaPropertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            var request = new IFindPropertyService.FindByIdRequestModel(propertyId);
            findPropertyService.findById(findByIdPresenter, request);

            assertThat(capturedPropertyResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Property not found");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when property ID is null")
        void shouldThrowIllegalArgumentExceptionWhenPropertyIdIsNull() {
            var request = new IFindPropertyService.FindByIdRequestModel(null);
            findPropertyService.findById(findByIdPresenter, request);

            assertThat(capturedPropertyResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PropertyId cannot be null");
        }
        @Test
        @DisplayName("Should handle generic exception during findById")
        void shouldHandleGenericExceptionDuringFindById() {
            UUID propertyId = UUID.randomUUID();
            when(jpaPropertyRepository.findById(propertyId)).thenThrow(new RuntimeException("Database error during findById"));

            var request = new IFindPropertyService.FindByIdRequestModel(propertyId);
            findPropertyService.findById(findByIdPresenter, request);

            assertThat(capturedPropertyResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error during findById");
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

        @Test
        @DisplayName("Should handle generic exception during findByPeriod")
        void shouldHandleGenericExceptionDuringFindByPeriod() {
            LocalDate startDate = LocalDate.of(2025, 5, 4);
            LocalDate endDate = startDate.plusDays(7);

            when(jpaPropertyRepository.findAvailablePropertiesByPeriod(startDate, endDate)).thenThrow(new RuntimeException("Database error during period search"));

            var request = new IFindPropertyService.PeriodRequestModel(startDate, endDate);
            findPropertyService.findByPeriod(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error during period search");
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

        @Test
        @DisplayName("Should handle generic exception during findByLocation")
        void shouldHandleGenericExceptionDuringFindByLocation() {
            String location = "São Paulo";
            when(jpaPropertyRepository.findByLocation(location)).thenThrow(new RuntimeException("Database error during location search"));

            var request = new IFindPropertyService.LocationRequestModel(location);
            findPropertyService.findByLocation(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error during location search");
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

        @Test
        @DisplayName("Should handle generic exception during findByPriceRange")
        void shouldHandleGenericExceptionDuringFindByPriceRange() {
            double min = 100.0;
            double max = 300.0;
            when(jpaPropertyRepository.findByDailyRateBetween(min, max)).thenThrow(new RuntimeException("Database error during price range search"));

            var request = new IFindPropertyService.PriceRangeRequestModel(min, max);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error during price range search");
        }
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

    @Nested
    @Tag("Mutation")
    @Tag("UnitTest")
    @DisplayName("Mutation Tests")
    class MutationTests {

        @Test
        @DisplayName("Should allow price range when min equals max")
        void shouldAllowPriceRangeWhenMinEqualsMax() {
            double min = 150.0;
            double max = 150.0;

            PropertyEntity e = factory.generatePropertyEntity();
            List<Property> expected = List.of(PropertyMapper.toDomain(e));

            when(jpaPropertyRepository.findByDailyRateBetween(min, max)).thenReturn(List.of(e));

            var request = new IFindPropertyService.PriceRangeRequestModel(min, max);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).containsExactlyElementsOf(expected);
        }

        @Test
        @DisplayName("Should allow zero values for price range")
        void shouldAllowZeroValuesForPriceRange() {
            double min = 0.0;
            double max = 0.0;

            PropertyEntity e = factory.generatePropertyEntity();
            List<Property> expected = List.of(PropertyMapper.toDomain(e));

            when(jpaPropertyRepository.findByDailyRateBetween(min, max)).thenReturn(List.of(e));

            var request = new IFindPropertyService.PriceRangeRequestModel(min, max);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).containsExactlyElementsOf(expected);
        }

        @Test
        @DisplayName("Should reject negative min and zero max")
        void shouldRejectNegativeMinWithZeroMax() {
            var request = new IFindPropertyService.PriceRangeRequestModel(-1.0, 0.0);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Prices must be non-negative");
        }

        @Test
        @DisplayName("Should reject zero min and negative max")
        void shouldRejectZeroMinWithNegativeMax() {
            var request = new IFindPropertyService.PriceRangeRequestModel(0.0, -1.0);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedResponse).isNull();
            assertThat(capturedException)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Prices must be non-negative");
        }
    }
}