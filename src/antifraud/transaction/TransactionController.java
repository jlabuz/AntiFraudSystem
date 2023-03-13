package antifraud.transaction;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {

    private final TransactionValidator transactionValidator;

    public TransactionController(TransactionValidator transactionValidator) {
        this.transactionValidator = transactionValidator;
    }

    @PostMapping("transaction")
    public ValidationResult processTransaction(@Valid @RequestBody Transaction transaction) {
        return new ValidationResult(transactionValidator.validate(transaction.getAmount()));
    }
}
