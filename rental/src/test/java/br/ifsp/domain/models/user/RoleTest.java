package br.ifsp.domain.models.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role Tests")
@Tag("Structural")
@Tag("UnitTest")
class RoleTest {

    @Nested
    @DisplayName("Role Enum Values")
    class EnumValuesTests {

        @ParameterizedTest(name = "Should retrieve Role.{0} from string ''{0}''")
        @CsvSource({
                "USER",
                "ADMIN"
        })
        void shouldRetrieveRoleFromString(String roleName) {
            Role role = Role.valueOf(roleName);
            assertNotNull(role, "The role should not be null");
            assertEquals(roleName, role.name(), "The retrieved role name should match the input string");
        }

        @ParameterizedTest(name = "Should have correct ordinal for {0}")
        @CsvSource({
                "USER, 0",
                "ADMIN, 1"
        })
        void shouldHaveCorrectOrdinal(String roleName, int ordinal) {
            Role role = Role.valueOf(roleName);
            assertEquals(ordinal, role.ordinal(), "The ordinal of the role should be correct");
        }
    }
}