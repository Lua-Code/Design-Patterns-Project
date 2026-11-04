package decorator;

public class TaxInfoDecorator extends ReceiptDecorator {

    public TaxInfoDecorator(Receipt receipt) {
        super(receipt);
    }

    @Override
    public String generateReceipt() {
        return super.generateReceipt()
                + "\n===== TAX INFORMATION =====\n"
                + "This receipt may be kept for financial records.\n"
                + "Tax deductibility depends on applicable laws and regulations.\n"
                + "===========================\n";
    }
}
