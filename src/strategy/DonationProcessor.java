
package strategy;

public class DonationProcessor {

    private DonationStrategy strategy;

    public DonationProcessor(DonationStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(DonationStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean processDonation(double amount) {
        if (strategy == null) {
            System.out.println("No donation method selected!");
            return false;
        }

        strategy.collectDonationDetails();
        return strategy.donate(amount);
    }
}
