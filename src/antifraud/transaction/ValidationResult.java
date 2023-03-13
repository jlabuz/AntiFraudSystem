package antifraud.transaction;

import java.util.Objects;

public class ValidationResult {
    private TransactionStatus result;

    public ValidationResult(TransactionStatus result) {
        this.result = result;
    }

    public ValidationResult() {
    }

    public TransactionStatus getResult() {
        return result;
    }

    public void setResult(TransactionStatus result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult result1 = (ValidationResult) o;
        return result == result1.result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(result);
    }
}
