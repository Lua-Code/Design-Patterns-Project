
package model;
import java.io.Serializable;

public class Donation implements Serializable{

    private int donationId;
    private Donor donor;
    private double amount;
    private String donationType;
    private String donationMethod;
    private String itemName;
    private int quantity;

    public Donation(int donationId, Donor donor, double amount,
                    String donationType, String donationMethod) {
        this.donationId = donationId;
        this.donor = donor;
        this.amount = amount;
        this.donationType = donationType;
        this.donationMethod = donationMethod;
        this.itemName = "";
        this.quantity = 0;
    }

    public int getDonationId() {
        return donationId;
    }

    public Donor getDonor() {
        return donor;
    }

    public double getAmount() {
        return amount;
    }

    public String getDonationType() {
        return donationType;
    }

    public String getDonationMethod() {
        return donationMethod;
    }


    public void setInKindDetails(String itemName, int quantity) {

        if (!donationMethod.equalsIgnoreCase("In-Kind")) {
            throw new IllegalStateException(
                    "Item details are only allowed for in-kind donations."
            );
        }

        if (itemName == null || itemName.isBlank() || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Please enter a valid item name and quantity."
            );
        }

        this.itemName = itemName;
        this.quantity = quantity;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void displayInfo() {
        System.out.println("Donation ID: " + donationId);
        System.out.println("Donor: " + donor.getName());
        System.out.println("Amount: " + amount + " EGP");
        System.out.println("Donation Type: " + donationType);
        System.out.println("Donation Method: " + donationMethod);
    }

}
