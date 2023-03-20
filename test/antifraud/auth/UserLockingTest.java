package antifraud.auth;

import antifraud.ControllerTest;
import antifraud.auth.dto.Operation;
import antifraud.auth.dto.UserLockRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserLockingTest extends ControllerTest {
    private static final String USER_LOCK_ENDPOINT = "/api/auth/access";
    private static final UserLockRequest DEFAULT_REQUEST = new UserLockRequest("someuser", Operation.LOCK);

    @Test
    void shouldReturnUnauthorizedForAnonymousUserLocking() {
        var request = buildAnonymousUserLockRequest(DEFAULT_REQUEST);
        var response = request.exchange();
        response.expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @ValueSource(strings = {MERCHANT_USERNAME, SUPPORT_USERNAME})
    void shouldReturnUnauthorizedForMerchantOrSupportLockingUser(String username) {
        var request = buildUserLockRequestAsUser(DEFAULT_REQUEST, username);
        var response = request.exchange();
        response.expectStatus().isForbidden();
    }

    @Test
    void shouldReturnNotFoundForLockingOfNonexistentUser() {
        var request = buildUserLockRequestAsAdmin(new UserLockRequest("nonexistentuser", Operation.LOCK));
        var response = request.exchange();
        response.expectStatus().isNotFound();
    }

    @Test
    void shouldReturnBadRequestForLockingAdministrator() {
        var request = buildUserLockRequestAsAdmin(new UserLockRequest(ADMIN_USERNAME, Operation.LOCK));
        var response = request.exchange();
        response.expectStatus().isBadRequest();
    }

    @Test
    void shouldLockExistingUserAndReturnStatus() {
        String username = "to_lock";
        var userToChange = new User("To Lock", username, "password", Role.MERCHANT, false);
        userRepository.save(userToChange);
        var request = buildUserLockRequestAsAdmin(new UserLockRequest(username, Operation.LOCK));

        var response = request.exchange();

        response
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo(String.format("User %s locked!", username));

        Optional<User> user = userRepository.findByUsername(username);
        assertTrue(user.isPresent());
        assertTrue(user.get().isLocked());
    }

    @Test
    void shouldUnlockExistingUserAndReturnStatus() {
        String username = "to_unlock";
        var userToChange = new User("To Unlock", username, "password", Role.MERCHANT, true);
        userRepository.save(userToChange);
        var request = buildUserLockRequestAsAdmin(new UserLockRequest(username, Operation.UNLOCK));

        var response = request.exchange();

        response
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo(String.format("User %s unlocked!", username));

        Optional<User> user = userRepository.findByUsername(username);
        assertTrue(user.isPresent());
        assertFalse(user.get().isLocked());

    }

    private WebTestClient.RequestHeadersSpec<?> buildUserLockRequestAsAdmin(UserLockRequest request) {
        return buildUserLockRequestAsUser(request, ADMIN_USERNAME);
    }

    private WebTestClient.RequestHeadersSpec<?> buildUserLockRequestAsUser(UserLockRequest request, String user) {
        return buildAnonymousUserLockRequest(request)
                .headers(headers -> headers.setBasicAuth(user, PASSWORD));
    }

    private WebTestClient.RequestHeadersSpec<?> buildAnonymousUserLockRequest(UserLockRequest request) {
        return this.webClient
                .put()
                .uri(USER_LOCK_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request);
    }
}
