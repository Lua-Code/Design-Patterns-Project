package strategy;

public class OnlineDonation implements DonationStrategy {

    private String email;
    private String paymentReference;
    private boolean detailsCollected = false;

    // Constructor receives information from the GUI
    public OnlineDonation(String email, String paymentReference) {
        this.email = email;
        this.paymentReference = paymentReference;
    }

    @Override
    public void collectDonationDetails() {

        if (email != null && paymentReference != null
                && !email.isBlank() && !paymentReference.isBlank()) {

            detailsCollected = true;
            System.out.println("Online payment details collected successfully.");

        } else {
            detailsCollected = false;
            System.out.println("Invalid online payment details!");
        }
    }

    @Override
    public boolean donate(double amount) {

        if (detailsCollected && Double.isFinite(amount) && amount > 0) {

            System.out.println(
                    "Online donation of " + amount + " EGP successful!"
            );

            return true;

        } else {

            System.out.println("Online donation failed!");
            return false;
        }
    }
}
