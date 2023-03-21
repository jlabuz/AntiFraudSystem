package antifraud.auth;

import antifraud.ip.IP;
import antifraud.ip.IPRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IPControllerTest {
    private static final String ANTIFRAUD_TRANSACTION_ENDPOINT = "/api/antifraud/suspicious-ip";
    private static final String MERCHANT_USERNAME = "merchant";
    private static final String ADMIN_USERNAME = "admin";
    private static final String SUPPORT_USERNAME = "support";
    private static final String PASSWORD = "password";

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private IPRepository ipRepository;

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

    @AfterEach
    void tearDown() {
        ipRepository.deleteAll();
    }

    @Test
    void shouldReturnUnauthorizedForAnonymousAddingIP() {
        var request = webClient.post().uri(ANTIFRAUD_TRANSACTION_ENDPOINT);
        var response = request.exchange();
        response.expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @ValueSource(strings = {MERCHANT_USERNAME, ADMIN_USERNAME})
    void shouldReturnForbiddenForNotSupportAddingIP(String username) {
        var request = webClient.post()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .headers(http -> http.setBasicAuth(username, PASSWORD));
        var response = request.exchange();
        response.expectStatus().isForbidden();
    }

    @Test
    void shouldReturnOkAndIdForSupportAddingIP() {
        String ip = "10.10.0.1";

        var response = webClient.post()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .headers(http -> http.setBasicAuth(SUPPORT_USERNAME, PASSWORD))
                .bodyValue("{\"ip\": \"" + ip + "\"}")
                .exchange();

        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.ip").isEqualTo(ip);
        assertTrue(ipRepository.findByIp(ip).isPresent());

    }

    @Test
    void shouldReturnBadRequestForAddingWrongIPFormat() {
        String ip = "12.wrongip.266.-9";

        var response = webClient.post()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .headers(http -> http.setBasicAuth(SUPPORT_USERNAME, PASSWORD))
                .bodyValue("{\"ip\": \"" + ip + "\"}")
                .exchange();

        response.expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnConflictForIPAlreadyInDatabase() {
        String ip = "10.10.0.1";
        ipRepository.save(new IP(ip));

        var response = webClient.post()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .headers(http -> http.setBasicAuth(SUPPORT_USERNAME, PASSWORD))
                .bodyValue("{\"ip\": \"" + ip + "\"}")
                .exchange();

        response.expectStatus().isEqualTo(HttpStatus.CONFLICT);
        assertFalse(ipRepository.findByIp(ip).isPresent());
    }

    @Test
    void shouldReturnUnauthorizedForAnonymousDeletingIP() {
        var response = webClient.delete()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .exchange();
        response.expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @ValueSource(strings = {MERCHANT_USERNAME, ADMIN_USERNAME})
    void shouldReturnForbiddenForNotSupportDeletingIP(String username) {
        var response = webClient.delete()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .headers(http -> http.setBasicAuth(username, PASSWORD))
                .exchange();
        response.expectStatus().isForbidden();
    }

    @Test
    void shouldReturnOkAndStatusMessageForSupportDeletingIP() {
        String ip = "10.10.0.1";
        ipRepository.save(new IP(ip));

        var response = webClient.delete()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT + "/" + ip)
                .headers(http -> http.setBasicAuth(SUPPORT_USERNAME, PASSWORD))
                .exchange();

        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("IP " + ip + "successfully removed!");
        assertFalse(ipRepository.findByIp(ip).isPresent());
    }

    @Test
    void shouldReturnBadRequestForRemovingWrongIPFormat() {
        String ip = "12.wrongip.266.-9";

        var response = webClient.delete()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT + "/" + ip)
                .headers(http -> http.setBasicAuth(SUPPORT_USERNAME, PASSWORD))
                .exchange();

        response.expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForRemovingIPNotInDatabase() {
        String ip = "0.0.0.0";

        var response = webClient.delete()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT + "/" + ip)
                .headers(http -> http.setBasicAuth(SUPPORT_USERNAME, PASSWORD))
                .exchange();

        response.expectStatus().isNotFound();
    }

    @Test
    void shouldReturnUnauthorizedForAnonymousListingIPs() {
        var request = webClient.get().uri(ANTIFRAUD_TRANSACTION_ENDPOINT);
        var response = request.exchange();
        response.expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @ValueSource(strings = {MERCHANT_USERNAME, ADMIN_USERNAME})
    void shouldReturnForbiddenForNotSupportListingIPs(String username) {
        var request = webClient.get()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .headers(http -> http.setBasicAuth(username, PASSWORD));
        var response = request.exchange();
        response.expectStatus().isForbidden();
    }

    @Test
    void shouldReturnOkAndListIPsInAscendingOrderForSupport() {
        List<String> ips = List.of("192.168.0.1", "192.168.0.2", "192.168.0.3");
        ipRepository.saveAll(ips.stream().map(IP::new).collect(Collectors.toList()));

        var response = webClient.get()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .headers(http -> http.setBasicAuth(SUPPORT_USERNAME, PASSWORD))
                .exchange();

        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$..id").isEqualTo(List.of(1, 2, 3))
                .jsonPath("$..ip").isEqualTo(ips);
    }

    @Test
    void shouldReturnOkAndEmptyListWhenNoIPsInDatabase() {
        var response = webClient.get()
                .uri(ANTIFRAUD_TRANSACTION_ENDPOINT)
                .headers(http -> http.setBasicAuth(SUPPORT_USERNAME, PASSWORD))
                .exchange();

        response.expectStatus().isOk()
                .expectBody().jsonPath("$").isArray().isEmpty();
    }
}
