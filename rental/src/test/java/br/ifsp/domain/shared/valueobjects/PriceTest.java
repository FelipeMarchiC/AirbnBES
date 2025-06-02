package br.ifsp.domain.shared.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("Structural")
@Tag("UnitTest")
@DisplayName("Price Value Object Tests")
class PriceTest {
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create Price with a positive amount")
        void shouldCreatePriceWithPositiveAmount() {
            BigDecimal amount = new BigDecimal("10.50");
            Price price = new Price(amount);
            assertThat(price).isNotNull();
            assertThat(price.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("Should create Price with zero amount")
        void shouldCreatePriceWithZeroAmount() {
            BigDecimal amount = BigDecimal.ZERO;
            Price price = new Price(amount);
            assertThat(price).isNotNull();
            assertThat(price.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when amount is null")
        void shouldThrowExceptionWhenAmountIsNull() {
            BigDecimal amount = null;
            assertThatThrownBy(() -> new Price(amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Price cannot be null or negative.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when amount is negative")
        void shouldThrowExceptionWhenAmountIsNegative() {
            BigDecimal amount = new BigDecimal("-5.00");
            assertThatThrownBy(() -> new Price(amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Price cannot be null or negative.");
        }
    }

    @Nested
    @DisplayName("Equals Method Tests")
    class EqualsMethodTests {

        @Test
        @DisplayName("Should return true when comparing with itself")
        void shouldReturnTrueWhenComparingWithItself() {
            Price price = new Price(new BigDecimal("10.00"));
            assertThat(price).isEqualTo(price);
        }

        @Test
        @DisplayName("Should return false when comparing with null")
        void shouldReturnFalseWhenComparingWithNull() {
            Price price = new Price(new BigDecimal("10.00"));
            assertThat(price).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should return false when comparing with different class object")
        void shouldReturnFalseWhenComparingWithDifferentClassObject() {
            Price price = new Price(new BigDecimal("10.00"));
            Object otherObject = new Object();
            assertThat(price).isNotEqualTo(otherObject);
        }

        @Test
        @DisplayName("Should return true when amounts are equal")
        void shouldReturnTrueWhenAmountsAreEqual() {
            Price price1 = new Price(new BigDecimal("10.00"));
            Price price2 = new Price(new BigDecimal("10.00"));
            assertThat(price1).isEqualTo(price2);
        }

        @Test
        @DisplayName("Should return true when amounts are equal but different scale")
        void shouldReturnTrueWhenAmountsAreEqualButDifferentScale() {
            Price price1 = new Price(new BigDecimal("10.0"));
            Price price2 = new Price(new BigDecimal("10.00"));
            assertThat(price1).isEqualTo(price2);
        }

        @Test
        @DisplayName("Should return false when amounts are different")
        void shouldReturnFalseWhenAmountsAreDifferent() {
            Price price1 = new Price(new BigDecimal("10.00"));
            Price price2 = new Price(new BigDecimal("20.00"));
            assertThat(price1).isNotEqualTo(price2);
        }
    }

    @Nested
    @DisplayName("HashCode Method Tests")
    class HashCodeMethodTests {

        @Test
        @DisplayName("Should return same hash code for equal objects")
        void shouldReturnSameHashCodeForEqualObjects() {
            Price price1 = new Price(new BigDecimal("10.00"));
            Price price2 = new Price(new BigDecimal("10.00"));
            assertThat(price1.hashCode()).isEqualTo(price2.hashCode());
        }

        @Test
        @DisplayName("Should return same hash code for equal objects with different scale")
        void shouldReturnSameHashCodeForEqualObjectsWithDifferentScale() {
            Price price1 = new Price(new BigDecimal("10.0"));
            Price price2 = new Price(new BigDecimal("10.00"));
            assertThat(price1.hashCode()).isEqualTo(price2.hashCode());
        }

        @Test
        @DisplayName("Should return different hash code for different objects")
        void shouldReturnDifferentHashCodeForDifferentObjects() {
            Price price1 = new Price(new BigDecimal("10.00"));
            Price price2 = new Price(new BigDecimal("20.00"));
            assertThat(price1.hashCode()).isNotEqualTo(price2.hashCode());
        }
    }

    @Nested
    @DisplayName("Getter and ToString Tests")
    class GetterAndToStringTests {

        @Test
        @DisplayName("Should return the correct amount via getAmount()")
        void shouldReturnCorrectAmount() {
            BigDecimal expectedAmount = new BigDecimal("123.45");
            Price price = new Price(expectedAmount);
            BigDecimal actualAmount = price.getAmount();
            assertThat(actualAmount).isEqualTo(expectedAmount);
        }

    }

}