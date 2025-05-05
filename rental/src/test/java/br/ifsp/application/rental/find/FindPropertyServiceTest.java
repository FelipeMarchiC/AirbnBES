package br.ifsp.application.rental.find;
import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.property.find.FindPropertyPresenter;
import br.ifsp.application.property.find.FindPropertyService;
import br.ifsp.application.property.find.IFindPropertyService;
import br.ifsp.application.rental.util.TestDataFactory;
import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.rental.Rental;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindPropertyServiceTest {

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
    class PropertySearchByPeriod{
        @Tag("TDD")
        @Tag("UnitTest")
        @Test
        @DisplayName("Should find properties available in Period")
        void shouldReturnAvailablePropertiesInPeriod(){
            Property property = factory.generateProperty();
            Property property2 = factory.generateProperty();

            Rental rental = factory.generateRental();
            LocalDate startDate = LocalDate.of(2025, 5, 4);
            rental.setStartDate(startDate);
            LocalDate endDate = startDate.plusDays(7);
            rental.setEndDate(endDate);

            Rental rental2 = factory.generateRental();
            rental.setStartDate(LocalDate.of(2025,5,8));
            rental.setEndDate(LocalDate.of(2025,5,11));

            property.addRental(rental);
            property2.addRental(rental2);

            rental.setProperty(property);
            rental2.setProperty(property2);

            var request =new IFindPropertyService.PeriodRequestModel(startDate,endDate);
            when(jpaPropertyRepository.findAvailablePropertiesByPeriod(startDate,endDate)).thenReturn(List.of(property));
            findPropertyService.findByPeriod(presenter,request);
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedException).isNull();
            verify(jpaPropertyRepository).findAvailablePropertiesByPeriod(startDate,endDate);

        }

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
