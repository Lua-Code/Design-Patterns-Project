
package decorator;

public abstract class ReceiptDecorator implements Receipt {

    protected Receipt wrappedReceipt;

    public ReceiptDecorator(Receipt receipt) {
        this.wrappedReceipt = receipt;
    }

    @Override
    public String generateReceipt() {
        return wrappedReceipt.generateReceipt();
    }
}
