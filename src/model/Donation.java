
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
    private String paymentEmail;
    private String paymentReference;
    private String bankName;
    private String checkNumber;
    private String accountHolder;
    private boolean taxInfoSelected;

    public Donation(int donationId, Donor donor, double amount,
                    String donationType, String donationMethod) {
        this.donationId = donationId;
        this.donor = donor;
        this.amount = amount;
        this.donationType = donationType;
        this.donationMethod = donationMethod;
        this.itemName = "";
        this.quantity = 0;
        this.paymentEmail = "";
        this.paymentReference = "";
        this.bankName = "";
        this.checkNumber = "";
        this.accountHolder = "";
        this.taxInfoSelected = false;
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

    public void setOnlineDetails(String paymentEmail, String paymentReference) {
        if (!"Online".equalsIgnoreCase(donationMethod)) {
            throw new IllegalStateException("Online details are only allowed for online donations.");
        }

        this.paymentEmail = paymentEmail;
        this.paymentReference = paymentReference;
    }

    public String getPaymentEmail() {
        return paymentEmail;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setCheckDetails(String bankName, String checkNumber, String accountHolder) {
        if (!"Check".equalsIgnoreCase(donationMethod)) {
            throw new IllegalStateException("Check details are only allowed for check donations.");
        }

        this.bankName = bankName;
        this.checkNumber = checkNumber;
        this.accountHolder = accountHolder;
    }

    public String getBankName() {
        return bankName;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setTaxInfoSelected(boolean taxInfoSelected) {
        this.taxInfoSelected = taxInfoSelected;
    }

    public boolean isTaxInfoSelected() {
        return taxInfoSelected;
    }


    public void setInKindDetails(String itemName, int quantity) {

        if (!"In-Kind".equalsIgnoreCase(donationMethod)) {
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
