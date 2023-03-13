package antifraud;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TransactionControllerTest {
    private static final String ANTIFRAUD_TRANSACTION_ENDPOINT = "/api/antifraud/transaction";

    @Autowired
    private WebTestClient webClient;

    @ParameterizedTest
    @ValueSource(ints = {50, 150, 200})
    void shouldAcceptLowAmountAndReturnAllowedStatus(int amount) {
        Transaction transaction = new Transaction(amount);
        ValidationResult result = new ValidationResult(TransactionStatus.ALLOWED);
        WebTestClient.RequestHeadersSpec<?> request = buildTransactionRequest(transaction);

        WebTestClient.ResponseSpec response = request.exchange();

        response
                .expectStatus().isOk()
                .expectBody(ValidationResult.class).isEqualTo(result);
    }

    @ParameterizedTest
    @ValueSource(ints = {201, 870, 1500})
    void shouldReturnManualProcessingStatusForIntermediateAmount(int amount) {
        Transaction transaction = new Transaction(amount);
        ValidationResult expectedResult = new ValidationResult(TransactionStatus.MANUAL_PROCESSING);
        WebTestClient.RequestHeadersSpec<?> request = buildTransactionRequest(transaction);

        WebTestClient.ResponseSpec response = request.exchange();

        response
                .expectStatus().isOk()
                .expectBody(ValidationResult.class).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(ints = {1501, 1700})
    void shouldReturnProhibitedStatusForTooLargeAmount(int amount) {
        Transaction transaction = new Transaction(amount);
        ValidationResult expectedResult = new ValidationResult(TransactionStatus.PROHIBITED);
        WebTestClient.RequestHeadersSpec<?> request = buildTransactionRequest(transaction);

        WebTestClient.ResponseSpec response = request.exchange();

        response
                .expectStatus().isOk()
                .expectBody(ValidationResult.class).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(ints = {-4, 0})
    void shouldReturnBadRequestForNegativeOrZeroAmount(int amount) {
        Transaction transaction = new Transaction(amount);
        WebTestClient.RequestHeadersSpec<?> request = buildTransactionRequest(transaction);

        WebTestClient.ResponseSpec response = request.exchange();

        response
                .expectStatus().isBadRequest();
    }

    private WebTestClient.RequestHeadersSpec<?> buildTransactionRequest(Transaction transaction) {
        return this.webClient
                .post()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transaction);
    }
}