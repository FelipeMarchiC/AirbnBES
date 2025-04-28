package br.ifsp.domain.models.rental;

public class Rental {
    private RentalState state;
    public RentalState getState(){
        return state;
    }
    public void setState(RentalState state){
        this.state=state;

    }
}
