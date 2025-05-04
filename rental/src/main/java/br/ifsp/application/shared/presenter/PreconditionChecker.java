package br.ifsp.application.shared.presenter;

import br.ifsp.application.shared.exceptions.UnauthenticatedUserException;
import br.ifsp.domain.models.user.User;

public final class PreconditionChecker {

    private PreconditionChecker() {}

    public static void prepareIfFailsPreconditions(GenericPresenter<?> presenter, User user) {
        prepareIfUnauthorized(presenter, user);
    }

    public static void prepareIfUnauthorized(GenericPresenter<?> presenter, User user) {
        if (user == null) {
            presenter.prepareFailView(new UnauthenticatedUserException("Current user is not authenticated."));
        }
    }
}