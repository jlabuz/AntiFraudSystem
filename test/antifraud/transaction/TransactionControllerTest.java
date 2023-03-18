package antifraud.transaction;

import antifraud.auth.User;
import antifraud.auth.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TransactionControllerTest {
    private static final String UNAUTHORIZED = "unauthorized";
    private static final String ANTIFRAUD_TRANSACTION_ENDPOINT = "/api/antifraud/transaction";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Autowired
    private WebTestClient webClient;

    @BeforeAll
    static void registerUserForAuthorization(@Autowired UserRepository userRepository, @Autowired PasswordEncoder passwordEncoder) {
        userRepository.save(new User("Test User", USERNAME, passwordEncoder.encode(PASSWORD)));
    }

    @AfterAll
    static void tearDown(@Autowired UserRepository userRepository) {
        userRepository.deleteAll();
    }

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

    @Test
    @Tag(UNAUTHORIZED)
    void shouldReturnUnauthorizedForAnonymousUser() {
        Transaction transaction = new Transaction(50);
        WebTestClient.RequestHeadersSpec<?> request = this.webClient
                .post()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transaction);

        WebTestClient.ResponseSpec response = request.exchange();

        response
                .expectStatus().isUnauthorized();
    }

    private WebTestClient.RequestHeadersSpec<?> buildTransactionRequest(Transaction transaction) {
        return this.webClient
                .post()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .headers(httpHeaders -> httpHeaders.setBasicAuth(USERNAME, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transaction);
    }
}