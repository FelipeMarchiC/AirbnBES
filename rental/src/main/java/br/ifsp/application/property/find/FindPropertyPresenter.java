package br.ifsp.application.property.find;

public interface FindPropertyPresenter {
    void prepareSuccessView(IFindPropertyService.PropertyListResponseModel responseModel);
    void prepareFailView(Exception e);
}
