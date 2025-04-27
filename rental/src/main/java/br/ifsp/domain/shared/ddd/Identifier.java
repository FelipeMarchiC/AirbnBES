package br.ifsp.domain.shared.ddd;

public interface Identifier<T> {
    boolean validate();
    T value();
}