package br.ifsp.application.property.find;

public interface IFindPropertyService {
    record RequestModel(/* insert what you need to receive */) {}
    record ResponseModel(/* what you need to return */) {}
}
