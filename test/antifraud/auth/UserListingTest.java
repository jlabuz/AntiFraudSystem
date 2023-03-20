package antifraud.auth;

import antifraud.auth.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserListingTest extends UserControllerTest {
    private static final String LIST_USERS_ENDPOINT = "/api/auth/list";

    @Test
    void shouldReturnUserListForAuthorizedUser() {
        userRepository.deleteAll();
        String username = "first";
        String password = "password";
        var users = List.of(
                new User("First", username, passwordEncoder.encode(password), Role.ADMINISTRATOR),
                new User("Second", "second", "password", Role.MERCHANT),
                new User("Third", "third", "password", Role.MERCHANT));
        Iterable<User> registered = userRepository.saveAll(users);
        var responses = StreamSupport.stream(registered.spliterator(), false).
                map(UserDTO::mapUserToUserDTO)
                .collect(Collectors.toList());

        var response = buildListingUsersRequest(username, password)
                .exchange();

        response
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .consumeWith(body -> {
                    List<UserDTO> result = body.getResponseBody();
                    assertNotNull(result);
                    assertIterableEquals(result, responses);
                });

    }

    @Test
    void shouldReturnOkForSupportUserUser() {
        var request = buildListingUsersRequest(SUPPORT_USERNAME, PASSWORD);
        var response = request.exchange();
        response.expectStatus().isOk();
    }

    @Test
    void shouldReturnUnauthorizedForAnonymousListingUsers() {
        var request = buildAnonymousListingUsersRequest();
        var response = request.exchange();
        response.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnForbiddenForMerchantListingUsers() {
        var request = buildListingUsersRequest(MERCHANT_USERNAME, PASSWORD);
        var response = request.exchange();
        response.expectStatus().isForbidden();
    }

    private WebTestClient.RequestHeadersSpec<?> buildAnonymousListingUsersRequest() {
        return this.webClient
                .get()
                .uri(LIST_USERS_ENDPOINT);
    }

    private WebTestClient.RequestHeadersSpec<?> buildListingUsersRequest(String username, String password) {
        return buildAnonymousListingUsersRequest()
                .headers(headers -> headers.setBasicAuth(username, password));
    }
}
