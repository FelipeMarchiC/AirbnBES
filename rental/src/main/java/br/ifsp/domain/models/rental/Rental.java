package br.ifsp.domain.models.rental;

import br.ifsp.domain.models.property.Property;
import br.ifsp.domain.models.user.User;
import br.ifsp.domain.shared.valueobjects.Price;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

@Builder(builderMethodName = "internalBuilder")
public class Rental {

    @Getter
    private final UUID id;

    @Getter
    private final User user;

    @Getter
    private final Property property;

    @Getter
    private final LocalDate startDate;

    @Getter
    private final LocalDate endDate;

    @Getter
    private final Price value;

    @Getter
    @Setter
    private RentalState state;

    private static final EnumSet<RentalState> FINAL_STATES = EnumSet.of(
            RentalState.CANCELLED,
            RentalState.CONFIRMED,
            RentalState.DENIED,
            RentalState.EXPIRED
    );

    public static RentalBuilder builder() {
        return new RentalBuilder();
    }

    public static class RentalBuilder {
        private RentalState initialState;
        private Clock clock;

        public RentalBuilder state(RentalState state) {
            this.initialState = state;
            return this;
        }

        public RentalBuilder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Rental build() {
            Clock effectiveClock = (clock != null) ? clock : Clock.systemDefaultZone();
            RentalState resolvedState;

            if (shouldExpire(this.endDate, initialState, effectiveClock)) {
                resolvedState = RentalState.EXPIRED;
            } else {
                resolvedState = initialState;
            }

            return internalBuilder()
                    .id(this.id)
                    .user(this.user)
                    .property(this.property)
                    .startDate(this.startDate)
                    .endDate(this.endDate)
                    .value(this.value)
                    .state(resolvedState)
                    .build();
        }
    }

    private static boolean shouldExpire(LocalDate endDate, RentalState currentState, Clock clock) {
        return endDate.isBefore(LocalDate.now(clock)) && !FINAL_STATES.contains(currentState);
    }
}
