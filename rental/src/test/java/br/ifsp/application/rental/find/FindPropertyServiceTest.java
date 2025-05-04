package br.ifsp.application.rental.find;

import br.ifsp.application.property.JpaPropertyRepository;
import br.ifsp.application.property.find.FindPropertyService;
import br.ifsp.domain.models.property.Property;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindPropertyServiceTest {
    private JpaPropertyRepository jpaPropertyRepository;
    private FindPropertyService findPropertyService;

    @BeforeEach
    void setup() {
        jpaPropertyRepository = mock(JpaPropertyRepository.class);
        findPropertyService = new FindPropertyService(jpaPropertyRepository);
    }

    @Nested
    @DisplayName("Property Search By Location Tests")
    class PropertySearchByLocationTests {

        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should return all properties for a given location")
        @Test
        void shouldReturnAllPropertiesForGivenLocation() {
            String location = "São Paulo";
            List<Property> mockProperties = List.of(
                    mock(Property.class),
                    mock(Property.class)
            );
            when(jpaPropertyRepository.findByLocation(location)).thenReturn(mockProperties);
            List<Property> result = findPropertyService.findByLocation(location);
            assertThat(result).isEqualTo(mockProperties);
            verify(jpaPropertyRepository).findByLocation(location);
        }
    }
    @Nested
    @DisplayName("Null or Blank Input Validation Tests")
    class NullOrBlankInputValidationTests {

        @Tag("UnitTest")
        @DisplayName("Should throw exception when location is null")
        @Test
        void shouldThrowExceptionWhenLocationIsNull() {
            assertThatThrownBy(() -> findPropertyService.findByLocation(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("location cannot be null or blank");
        }

        @Tag("UnitTest")
        @DisplayName("Should throw exception when location is blank")
        @Test
        void shouldThrowExceptionWhenLocationIsBlank() {
            assertThatThrownBy(() -> findPropertyService.findByLocation("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("location cannot be null or blank");
        }
    }

    @Nested
    @DisplayName("Property Search By Price Range Tests")
    class PropertySearchByPriceRangeTests {

        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should return properties within given price range")
        @Test
        void shouldReturnPropertiesWithinGivenPriceRange() {
            double min = 100.0;
            double max = 300.0;

            List<Property> mockProperties = List.of(
                    mock(Property.class),
                    mock(Property.class)
            );

            when(jpaPropertyRepository.findByDailyRateBetween(min, max)).thenReturn(mockProperties);

            List<Property> result = findPropertyService.findByPriceRange(min, max);

            assertThat(result).isEqualTo(mockProperties);
            verify(jpaPropertyRepository).findByDailyRateBetween(min, max);
        }

        @Tag("UnitTest")
        @DisplayName("Should throw exception if min is greater than max")
        @Test
        void shouldThrowExceptionIfMinGreaterThanMax() {
            assertThatThrownBy(() -> findPropertyService.findByPriceRange(500.0, 100.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Minimum price cannot be greater than maximum price");
        }
    }

    @Nested
    @DisplayName("Property FindAll Tests")
    class PropertyFindAllTests {
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("Should return all properties from repository")
        @Test
        void shouldReturnAllProperties() {
            List<Property> mockProperties = List.of(
                    mock(Property.class),
                    mock(Property.class)
            );
            when(jpaPropertyRepository.findAll()).thenReturn(mockProperties);
            List<Property> result = findPropertyService.findAll();
            assertThat(result).isEqualTo(mockProperties);
            verify(jpaPropertyRepository).findAll();
        }
    }
}