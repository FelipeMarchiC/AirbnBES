package br.ifsp.application.shared.presenter;

import br.ifsp.application.shared.exceptions.EntityNotFoundException;
import br.ifsp.domain.models.user.User;

public final class PreconditionChecker {

    private PreconditionChecker() {}

    public static void prepareIfFailsPreconditions(GenericPresenter<?> presenter, User user) {
        prepareIfUserIsNull(presenter, user);
    }

    public static void prepareIfUserIsNull(GenericPresenter<?> presenter, User user) {
        if (user == null) {
            presenter.prepareFailView(new EntityNotFoundException("User does not exist"));
        }
    }
}