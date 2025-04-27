package br.ifsp.domain.shared.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Embeddable
@NoArgsConstructor
public class Price {

    private BigDecimal amount;

    public Price(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative.");
        }

        this.amount = amount;
    }

    @Override
    public String toString() {
        return "R$" + amount.toString();
    }
}