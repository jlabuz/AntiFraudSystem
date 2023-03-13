package antifraud.transaction;

import org.springframework.stereotype.Component;

@Component
class TransactionValidator {

    private static final int ALLOWED_MAX = 200;
    private static final int MANUAL_MAX = 1500;

    public TransactionStatus validate(long amount) {
        if (amount <= ALLOWED_MAX) {
            return TransactionStatus.ALLOWED;
        } else if (amount <= MANUAL_MAX) {
            return TransactionStatus.MANUAL_PROCESSING;
        } else {
            return TransactionStatus.PROHIBITED;
        }
    }
}
