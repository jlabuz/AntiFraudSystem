package antifraud.auth;

import antifraud.auth.dto.DeletionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;


public class UserDeletingTest extends UserControllerTest {
    private static final String DELETE_USER_ENDPOINT = "/api/auth/user";

    @Test
    void shouldReturnUnauthorizedForAnonymousDeletingUser() {
        var request = buildAnonymousDeletingUserRequest("anyuser");
        var response = request.exchange();
        response.expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @ValueSource(strings = {MERCHANT_USERNAME, SUPPORT_USERNAME})
    void shouldReturnUnauthorizedForMerchantOrSupportDeletingUser(String username) {
        var request = buildDeletingUserRequestAsUser("anyuser", username);
        var response = request.exchange();
        response.expectStatus().isForbidden();
    }

    @Test
    void shouldReturnNotFoundForRemovingNonexistentUser() {
        var request = buildDeletingUserRequestAsAdmin("nonexistentUsername");
        var response = request.exchange();
        response.expectStatus().isNotFound();
    }

    @Test
    void shouldRemoveExistingUserAndReturnStatus() {
        var usernameToDelete = "todelete";
        var userToDelete = new User("To Delete", usernameToDelete, "password", Role.MERCHANT);
        userRepository.save(userToDelete);

        var expectedResponse = new DeletionResponse(usernameToDelete, "Deleted successfully!");

        var response = buildDeletingUserRequestAsAdmin(usernameToDelete)
                .exchange();

        response
                .expectStatus().isOk()
                .expectBody(DeletionResponse.class)
                .consumeWith(body -> {
                    var result = body.getResponseBody();
                    assertNotNull(result);
                    assertEquals(expectedResponse, result);
                });
        assertTrue(userRepository.findByUsername(usernameToDelete).isEmpty());
    }

    private WebTestClient.RequestHeadersSpec<?> buildDeletingUserRequestAsAdmin(String usernameToDelete) {
        return buildDeletingUserRequestAsUser(usernameToDelete, ADMIN_USERNAME);
    }

    private WebTestClient.RequestHeadersSpec<?> buildDeletingUserRequestAsUser(String usernameToDelete, String user) {
        return buildAnonymousDeletingUserRequest(usernameToDelete)
                .headers(headers -> headers.setBasicAuth(user, PASSWORD));
    }

    private WebTestClient.RequestHeadersSpec<?> buildAnonymousDeletingUserRequest(String username) {
        return this.webClient
                .delete()
                .uri(DELETE_USER_ENDPOINT + "/" + username);
    }
}
