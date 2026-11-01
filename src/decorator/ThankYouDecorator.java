
package decorator;

public class ThankYouDecorator extends ReceiptDecorator {

    private String donorName;

    public ThankYouDecorator(Receipt receipt, String donorName) {
        super(receipt);
        this.donorName = donorName;
    }

    @Override
    public String generateReceipt() {
        return super.generateReceipt()
                + "\nThank you, " + donorName
                + ", for your generous donation!\n"
                + "Your contribution helps us make a difference.\n";
    }
}
