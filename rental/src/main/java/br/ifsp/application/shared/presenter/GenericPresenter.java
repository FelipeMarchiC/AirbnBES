package br.ifsp.application.shared.presenter;

public interface GenericPresenter <T> {
    void prepareSuccessView(T response);
    void prepareFailView(Throwable throwable);
    boolean isDone();
}