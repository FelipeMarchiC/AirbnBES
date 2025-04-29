package br.ifsp.domain.models.rental;

public class Rental {
    private String rentalID;
    private RentalState state;
    public RentalState getState(){
        return state;
    }
    public void setState(RentalState state){
        this.state=state;

    }
    public void setRentalID(String rentalID) {
        this.rentalID = rentalID;
    }

    public String getRentalID() {
        return rentalID;
    }
}
