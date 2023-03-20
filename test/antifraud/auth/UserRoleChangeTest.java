package antifraud.auth;

import antifraud.ControllerTest;
import antifraud.auth.dto.RoleChangeRequest;
import antifraud.auth.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserRoleChangeTest extends ControllerTest {
    private static final String ROLE_CHANGE_ENDPOINT = "/api/auth/role";
    private static final RoleChangeRequest DEFAULT_REQUEST = new RoleChangeRequest("someuser", "MERCHANT");

    @Test
    void shouldReturnUnauthorizedForAnonymousChangingRole() {
        var request = buildAnonymousRoleChangeRequest(DEFAULT_REQUEST);
        var response = request.exchange();
        response.expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @ValueSource(strings = {MERCHANT_USERNAME, SUPPORT_USERNAME})
    void shouldReturnUnauthorizedForMerchantOrSupportChangingRole(String username) {
        var request = buildRoleChangeRequestAsUser(DEFAULT_REQUEST, username);
        var response = request.exchange();
        response.expectStatus().isForbidden();
    }

    @Test
    void shouldReturnNotFoundForChangingRoleOfNonexistentUser() {
        var request = buildRoleChangeRequestAsAdmin(new RoleChangeRequest("nonexistentuser", "MERCHANT"));
        var response = request.exchange();
        response.expectStatus().isNotFound();
    }

    @Test
    void shouldReturnConflictForChangingRoleToAlreadyAssigned() {
        var request = buildRoleChangeRequestAsAdmin(new RoleChangeRequest(MERCHANT_USERNAME, "MERCHANT"));
        var response = request.exchange();
        response.expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturnBadRequestForChangingRoleToAdministrator() {
        var request = buildRoleChangeRequestAsAdmin(new RoleChangeRequest(MERCHANT_USERNAME, "ADMINISTRATOR"));
        var response = request.exchange();
        response.expectStatus().isBadRequest();
    }

    @Test
    void shouldChangeRoleOfExistingUserAndReturnStatus() {
        String username = "to_change";
        var userToChange = new User("To Change Role", username, "password", Role.MERCHANT);
        User user = userRepository.save(userToChange);
        var request = buildRoleChangeRequestAsAdmin(new RoleChangeRequest(username, "SUPPORT"));
        var expectedResponse = new UserDTO(user.getId(), user.getName(), username, "SUPPORT");

        var response = request.exchange();

        response
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .consumeWith(body -> {
                    var result = body.getResponseBody();
                    assertNotNull(result);
                    assertEquals(expectedResponse, result);
                    assertEquals(expectedResponse.getRole(), result.getRole());
                });

        Optional<User> changedUser = userRepository.findByUsername(username);
        assertTrue(changedUser.isPresent());
        assertEquals(Role.SUPPORT, changedUser.get().getRole());

    }

    private WebTestClient.RequestHeadersSpec<?> buildRoleChangeRequestAsAdmin(RoleChangeRequest request) {
        return buildRoleChangeRequestAsUser(request, ADMIN_USERNAME);
    }

    private WebTestClient.RequestHeadersSpec<?> buildRoleChangeRequestAsUser(RoleChangeRequest request, String user) {
        return buildAnonymousRoleChangeRequest(request)
                .headers(headers -> headers.setBasicAuth(user, PASSWORD));
    }

    private WebTestClient.RequestHeadersSpec<?> buildAnonymousRoleChangeRequest(RoleChangeRequest request) {
        return this.webClient
                .put()
                .uri(ROLE_CHANGE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request);
    }
}
