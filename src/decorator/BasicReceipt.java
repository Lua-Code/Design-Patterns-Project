
package decorator;

import model.Donation;

public class BasicReceipt implements Receipt {

    private Donation donation;

    public BasicReceipt(Donation donation) {
        this.donation = donation;
    }


    @Override
    public String generateReceipt() {

        String receipt = "===== DONATION RECEIPT =====\n"
                + "Donation ID: " + donation.getDonationId() + "\n"
                + "Donor: " + donation.getDonor().getName() + "\n"
                + "Donation Type: " + donation.getDonationType() + "\n"
                + "Donation Method: " + donation.getDonationMethod() + "\n";

        if (donation.getDonationMethod().equalsIgnoreCase("In-Kind")) {

            receipt += "Item: " + donation.getItemName() + "\n"
                    + "Quantity: " + donation.getQuantity() + "\n"
                    + "Estimated Value: " + donation.getAmount() + " EGP\n";

        } else {

            receipt += "Amount: " + donation.getAmount() + " EGP\n";
        }

        receipt += "==========================\n";

        return receipt;
    }

}
