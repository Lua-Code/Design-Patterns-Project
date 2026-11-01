
package strategy;

public class CheckDonation implements DonationStrategy {

    private String bankName;
    private String checkNumber;
    private String accountHolder;
    private boolean detailsCollected = false;

    // Constructor receives information from the GUI
    public CheckDonation(String bankName, String checkNumber,
                         String accountHolder) {

        this.bankName = bankName;
        this.checkNumber = checkNumber;
        this.accountHolder = accountHolder;
    }

    @Override
    public void collectDonationDetails() {

        if (bankName != null && checkNumber != null
                && accountHolder != null
                && !bankName.isBlank()
                && !checkNumber.isBlank()
                && !accountHolder.isBlank()) {

            detailsCollected = true;
            System.out.println("Check details collected successfully.");

        } else {

            detailsCollected = false;
            System.out.println("Invalid check details!");
        }
    }

    @Override
    public boolean donate(double amount) {

        if (detailsCollected && Double.isFinite(amount) && amount > 0) {

            System.out.println(
                    "Check donation of " + amount + " EGP accepted!"
            );

            return true;

        } else {

            System.out.println("Check donation failed!");
            return false;
        }
    }
}
