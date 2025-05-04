package br.ifsp.application.rental.find;
import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.property.find.FindPropertyPresenter;
import br.ifsp.application.property.find.FindPropertyService;
import br.ifsp.application.property.find.IFindPropertyService;
import br.ifsp.domain.models.property.Property;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindPropertyServiceTest {

    private JpaPropertyRepository jpaPropertyRepository;
    private FindPropertyService findPropertyService;
    private FindPropertyPresenter presenter;

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
            public void prepareFailView(Exception e) {
                capturedException = e;
            }
        };
        findPropertyService = new FindPropertyService(jpaPropertyRepository);
        capturedResponse = null;
        capturedException = null;
    }

    @Nested
    @DisplayName("Property Search By Location Tests")
    class PropertySearchByLocationTests {

        @Test
        @DisplayName("Should return all properties for a given location")
        void shouldReturnAllPropertiesForGivenLocation() {
            String location = "São Paulo";
            List<Property> mockProperties = List.of(mock(Property.class), mock(Property.class));
            when(jpaPropertyRepository.findByLocation(location)).thenReturn(mockProperties);

            var request = new IFindPropertyService.LocationRequestModel(location);
            findPropertyService.findByLocation(presenter, request);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).isEqualTo(mockProperties);
            verify(jpaPropertyRepository).findByLocation(location);
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
    class PropertySearchByPriceRangeTests {

        @Test
        @DisplayName("Should return properties within given price range")
        void shouldReturnPropertiesWithinGivenPriceRange() {
            double min = 100.0;
            double max = 300.0;
            List<Property> mockProperties = List.of(mock(Property.class), mock(Property.class));
            when(jpaPropertyRepository.findByDailyRateBetween(min, max)).thenReturn(mockProperties);

            var request = new IFindPropertyService.PriceRangeRequestModel(min, max);
            findPropertyService.findByPriceRange(presenter, request);

            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).isEqualTo(mockProperties);
            verify(jpaPropertyRepository).findByDailyRateBetween(min, max);
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
    }

    @Nested
    @DisplayName("Property FindAll Tests")
    class PropertyFindAllTests {

        @Test
        @DisplayName("Should return all properties from repository")
        void shouldReturnAllProperties() {
            List<Property> mockProperties = List.of(mock(Property.class), mock(Property.class));
            when(jpaPropertyRepository.findAll()).thenReturn(mockProperties);
            findPropertyService.findAll(presenter);
            assertThat(capturedException).isNull();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.properties()).isEqualTo(mockProperties);
            verify(jpaPropertyRepository).findAll();
        }
    }
}
