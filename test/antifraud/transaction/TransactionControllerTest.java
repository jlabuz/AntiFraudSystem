package antifraud.transaction;

import antifraud.auth.Role;
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
    private static final String ANTIFRAUD_TRANSACTION_ENDPOINT = "/api/antifraud/transaction";
    private static final String MERCHANT_USERNAME = "username";
    private static final String ADMIN_USERNAME = "admin";
    private static final String SUPPORT_USERNAME = "support";
    private static final String PASSWORD = "password";

    private static final Transaction DEFAULT_TRANSACTION = new Transaction(50);

    @Autowired
    private WebTestClient webClient;

    @BeforeAll
    static void registerUserForAuthorization(@Autowired UserRepository userRepository, @Autowired PasswordEncoder passwordEncoder) {
        userRepository.save(new User("Merchant", MERCHANT_USERNAME, passwordEncoder.encode(PASSWORD), Role.MERCHANT));
        userRepository.save(new User("Admin", ADMIN_USERNAME, passwordEncoder.encode(PASSWORD), Role.ADMINISTRATOR));
        userRepository.save(new User("Support", SUPPORT_USERNAME, passwordEncoder.encode(PASSWORD), Role.SUPPORT));
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
        var request = buildTransactionRequestAsMerchant(transaction);

        var response = request.exchange();

        response
                .expectStatus().isOk()
                .expectBody(ValidationResult.class).isEqualTo(result);
    }

    @ParameterizedTest
    @ValueSource(ints = {201, 870, 1500})
    void shouldReturnManualProcessingStatusForIntermediateAmount(int amount) {
        Transaction transaction = new Transaction(amount);
        ValidationResult expectedResult = new ValidationResult(TransactionStatus.MANUAL_PROCESSING);
        var request = buildTransactionRequestAsMerchant(transaction);

        var response = request.exchange();

        response
                .expectStatus().isOk()
                .expectBody(ValidationResult.class).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(ints = {1501, 1700})
    void shouldReturnProhibitedStatusForTooLargeAmount(int amount) {
        Transaction transaction = new Transaction(amount);
        ValidationResult expectedResult = new ValidationResult(TransactionStatus.PROHIBITED);
        var request = buildTransactionRequestAsMerchant(transaction);

        var response = request.exchange();

        response
                .expectStatus().isOk()
                .expectBody(ValidationResult.class).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(ints = {-4, 0})
    void shouldReturnBadRequestForNegativeOrZeroAmount(int amount) {
        var request = buildTransactionRequestAsMerchant(new Transaction(amount));
        var response = request.exchange();
        response.expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnUnauthorizedForAnonymousUser() {
        var request = buildAnonymousTransactionRequest(DEFAULT_TRANSACTION);
        var response = request.exchange();
        response.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnForbiddenForAdministrator() {
        var request = buildDefaultTransactionRequestAsUser(ADMIN_USERNAME);
        var response = request.exchange();
        response.expectStatus().isForbidden();
    }

    @Test
    void shouldReturnForbiddenForSupport() {
        var request = buildDefaultTransactionRequestAsUser(SUPPORT_USERNAME);
        var response = request.exchange();
        response.expectStatus().isForbidden();
    }

    private WebTestClient.RequestHeadersSpec<?> buildTransactionRequestAsMerchant(Transaction transaction) {
        return buildAnonymousTransactionRequest(transaction)
                .headers(httpHeaders -> httpHeaders.setBasicAuth(MERCHANT_USERNAME, PASSWORD));
    }

    private WebTestClient.RequestHeadersSpec<?> buildDefaultTransactionRequestAsUser(String username) {
        return buildAnonymousTransactionRequest(DEFAULT_TRANSACTION)
                .headers(httpHeaders -> httpHeaders.setBasicAuth(username, PASSWORD));
    }

    private WebTestClient.RequestHeadersSpec<?> buildAnonymousTransactionRequest(Transaction transaction) {
        return this.webClient
                .post()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transaction);
    }
}