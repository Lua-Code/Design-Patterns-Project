
package strategy;

public class InKindDonation implements DonationStrategy {

    private String itemName;
    private int quantity;
    private boolean detailsCollected = false;

    // Constructor receives information from the GUI
    public InKindDonation(String itemName, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
    }

    @Override
    public void collectDonationDetails() {

        if (itemName != null && !itemName.isBlank() && quantity > 0) {

            detailsCollected = true;
            System.out.println("In-kind donation details collected successfully.");

        } else {

            detailsCollected = false;
            System.out.println("Invalid item details!");
        }
    }

    @Override
    public boolean donate(double amount) {

        if (detailsCollected && Double.isFinite(amount) && amount >= 0) {

            System.out.println("In-kind donation accepted!");
            System.out.println("Item: " + itemName);
            System.out.println("Quantity: " + quantity);
            System.out.println("Estimated Value: " + amount + " EGP");

            return true;

        } else {

            System.out.println("In-kind donation failed!");
            return false;
        }
    }
}
