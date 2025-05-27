package br.ifsp.application.shared.presenter;

import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.domain.models.user.UserEntity;

import java.time.Clock;
import java.time.LocalDate;

public final class PreconditionChecker {

    private PreconditionChecker() {}

    public static void prepareIfFailsPreconditions(GenericPresenter<?> presenter, UserEntity userEntity) {
        prepareIfUserIsNull(presenter, userEntity);
    }

    public static void prepareIfUserIsNull(GenericPresenter<?> presenter, UserEntity userEntity) {
        if (userEntity == null) {
            presenter.prepareFailView(new EntityNotFoundException("User does not exist"));
        }
    }

    public static void prepareIfTheDateIsInThePast(
            GenericPresenter<?> presenter, Clock clock, LocalDate targetDate
    ) {
        if (targetDate.isBefore(LocalDate.now(clock))) {
            presenter.prepareFailView(new EntityNotFoundException("User does not exist"));
        }
    }
}