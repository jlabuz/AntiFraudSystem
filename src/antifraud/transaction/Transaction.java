package antifraud.transaction;

import javax.validation.constraints.Min;

class Transaction {
    @Min(value = 1)
    private long amount;

    public Transaction(long amount) {
        this.amount = amount;
    }

    public Transaction() {
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
