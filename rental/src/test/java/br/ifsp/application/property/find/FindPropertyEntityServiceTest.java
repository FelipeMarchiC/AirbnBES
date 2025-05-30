package br.ifsp.application.property.find;

import br.ifsp.application.property.repository.JpaPropertyRepository;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.domain.models.property.PropertyEntity;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindPropertyEntityServiceTest {

    private JpaPropertyRepository jpaPropertyRepository;
    private FindPropertyService findPropertyService;
    private FindPropertyPresenter presenter;
    private TestDataFactory factory;

    private IFindPropertyService.PropertyListResponseModel capturedResponse;
    private Exception capturedException;

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
        factory = new TestDataFactory();
        findPropertyService = new FindPropertyService(jpaPropertyRepository);
        capturedResponse = null;
        capturedException = null;
    }

    @Nested
    @DisplayName("Property search by period tests")
    class PropertyEntitySearchByPeriod {

        @Test
        @DisplayName("Should return available properties in period")
        void shouldReturnAvailablePropertiesInPeriod() {
            PropertyEntity property = factory.generatePropertyEntity();
            LocalDate startDate = LocalDate.of(2025, 5, 4);
            LocalDate endDate = startDate.plusDays(7);

            when(jpaPropertyRepository.findAvailablePropertiesByPeriod(startDate, endDate)).thenReturn(List.of(property));

            var request = new IFindPropertyService.PeriodRequestModel(startDate, endDate);
            findPropertyService.findByPeriod(presenter, request);

            assertThat(capturedResponse).isNotNull();
            assertThat(capturedException).isNull();
            assertThat(capturedResponse.properties()).containsExactly(property);
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
            List<PropertyEntity> mockProperties = List.of(mock(PropertyEntity.class), mock(PropertyEntity.class));
            when(jpaPropertyRepository.findByLocation(location)).thenReturn(mockProperties);

            var request = new IFindPropertyService.LocationRequestModel(location);
            findPropertyService.findByLocation(presenter, request);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).isEqualTo(mockProperties);
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
            List<PropertyEntity> mockProperties = List.of(mock(PropertyEntity.class), mock(PropertyEntity.class));
            when(jpaPropertyRepository.findByDailyRateBetween(min, max)).thenReturn(mockProperties);

            var request = new IFindPropertyService.PriceRangeRequestModel(min, max);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).isEqualTo(mockProperties);
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
    }

    @Nested
    @DisplayName("Property FindAll Tests")
    class PropertyEntityFindAllTests {

        @Test
        @DisplayName("Should return all properties from repository")
        void shouldReturnAllProperties() {
            List<PropertyEntity> mockProperties = List.of(mock(PropertyEntity.class), mock(PropertyEntity.class));
            when(jpaPropertyRepository.findAll()).thenReturn(mockProperties);
            findPropertyService.findAll(presenter);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).isEqualTo(mockProperties);
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