package br.ifsp.application.rental.find;

import java.util.UUID;

public interface IFindRentalService {
    record RequestModel(/* what you need to receive */) {}
    record ResponseModel(/* what you need to return */) {}
}
