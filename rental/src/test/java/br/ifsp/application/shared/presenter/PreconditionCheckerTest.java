package br.ifsp.application.shared.presenter;

import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.application.shared.exceptions.ImmutablePastEntityException;
import br.ifsp.domain.models.user.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.mockito.Mockito.*;

@Tag("Structural")
@Tag("UnitTest")
class PreconditionCheckerTest {

    @Nested
    class PrepareIfUserIsNullTests {
        @Test
        void shouldHandleNullUser() {
            GenericPresenter<?> presenter = mock(GenericPresenter.class);
            PreconditionChecker.prepareIfUserIsNull(presenter, null);
            verify(presenter, times(1)).prepareFailView(any(EntityNotFoundException.class));
        }

        @Test
        void shouldHandleNonNullUser() {
            GenericPresenter<?> presenter = mock(GenericPresenter.class);
            User user = mock(User.class);
            PreconditionChecker.prepareIfUserIsNull(presenter, user);
            verify(presenter, never()).prepareFailView(any(EntityNotFoundException.class));
        }
    }

    @Nested
    class PrepareIfTheDateIsInThePastTests {
        @Test
        void shouldHandlePastDate() {
            GenericPresenter<?> presenter = mock(GenericPresenter.class);

            LocalDate tomorrow = LocalDate.now().plusDays(1);
            Clock fixedClock = Clock.fixed(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

            PreconditionChecker.prepareIfTheDateIsInThePast(presenter, fixedClock, LocalDate.now());

            verify(presenter, times(1)).prepareFailView(any(ImmutablePastEntityException.class));
        }

        @Test
        void shouldHandleCurrentOrFutureDate() {
            GenericPresenter<?> presenter = mock(GenericPresenter.class);
            LocalDate today = LocalDate.now();
            Clock fixedClock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

            PreconditionChecker.prepareIfTheDateIsInThePast(presenter, fixedClock, LocalDate.now());
            verify(presenter, never()).prepareFailView(any(ImmutablePastEntityException.class));
        }
    }

    @Nested
    class PrepareIfFailsPreconditionsTests {
        @Test
        void shouldCallPrepareIfUserIsNull() {
            GenericPresenter<?> presenter = mock(GenericPresenter.class);
            PreconditionChecker.prepareIfFailsPreconditions(presenter, null);
            verify(presenter, times(1)).prepareFailView(any(EntityNotFoundException.class));
        }
    }
}