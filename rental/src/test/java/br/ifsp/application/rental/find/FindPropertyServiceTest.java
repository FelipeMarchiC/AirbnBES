package br.ifsp.application.rental.find;

import br.ifsp.application.property.JpaPropertyRepository;
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
}