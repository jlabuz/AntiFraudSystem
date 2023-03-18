package antifraud.auth;

import antifraud.auth.dto.DeletionResponse;
import antifraud.auth.dto.RegisterRequest;
import antifraud.auth.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    private static final String USER_ENDPOINT = "/api/auth/user";
    private static final String USER_LIST_ENDPOINT = "/api/auth/list";
    private static final String DELETE_USER_ENDPOINT = "/api/auth/user";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserAndReturnCreated() {
        String name = "John Doe";
        String username = "johndoe";
        RegisterRequest request = new RegisterRequest(name, username, "secretpassword");

        var response = buildRegistrationRequest(request).exchange();

        response
                .expectStatus().isCreated()
                .expectBody(UserDTO.class)
                .consumeWith(body -> {
                    UserDTO result = body.getResponseBody();
                    assertNotNull(result);
                    assertEquals(username, result.getUsername());
                    assertEquals(name, result.getName());
                });
        assertTrue(userRepository.findByUsername(username).isPresent());
    }

    @Test
    void shouldReturnConflictForExistingUsername() {
        String name = "Registered User";
        String username = "registered";
        String password = "password";
        userRepository.save(new User(name, username, password));
        RegisterRequest request = new RegisterRequest(name, username, password);

        var response = buildRegistrationRequest(request).exchange();

        response.expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @ParameterizedTest
    @CsvSource({
            ", username, password",
            "name, , password",
            "name, username, "
    })
    @MethodSource("badRegisterRequests")
    void shouldReturnBadRequestForEmptyFieldInRegisterRequest(String name, String username, String password) {
        RegisterRequest request = new RegisterRequest(name, username, password);

        var response = buildRegistrationRequest(request).exchange();

        response.expectStatus().isBadRequest();
    }

    private static Stream<Arguments> badRegisterRequests() {
        return Stream.of(
                arguments("", "username", "password"),
                arguments(null, "username", "password"),
                arguments("name", "", "password"),
                arguments("name", null, "password"),
                arguments("username", "password", ""),
                arguments("username", "password", null)
        );
    }

    private WebTestClient.RequestHeadersSpec<?> buildRegistrationRequest(RegisterRequest request) {
        return this.webClient
                .post()
                .uri(USER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request);
    }

    @Test
    void shouldReturnUserListForAuthorizedUser() {
        String username = "first";
        String password = "password";
        var users = List.of(
                new User("First", username, passwordEncoder.encode(password)),
                new User("Second", "second", "password"),
                new User("Third", "third", "password"));
        Iterable<User> registered = userRepository.saveAll(users);
        var responses = StreamSupport.stream(registered.spliterator(), false).
                map(u -> new UserDTO(u.getId(), u.getName(), u.getUsername()))
                .collect(Collectors.toList());

        var response = this.webClient
                .get()
                .uri(USER_LIST_ENDPOINT)
                .headers(headers -> headers.setBasicAuth(username, password))
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
    void shouldReturnUnauthorizedForAnonymousListingUsers() {
        var response = this.webClient
                .get()
                .uri(USER_LIST_ENDPOINT)
                .exchange();

        response.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnUnauthorizedForAnonymousDeletingUser() {
        var response = this.webClient
                .delete()
                .uri(DELETE_USER_ENDPOINT + "/nonexistent")
                .exchange();

        response.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnNotFountForRemovingNonexistentUser() {
        String username = "username";
        String password = "password";

        var user = new User("User", "username", passwordEncoder.encode(password));
        userRepository.save(user);

        var response = this.webClient
                .delete()
                .uri(DELETE_USER_ENDPOINT + "/nonexistent")
                .headers(headers -> headers.setBasicAuth(username, password))
                .exchange();

        response.expectStatus().isNotFound();
    }

    @Test
    void shouldRemoveExistingUserAndReturnStatus() {
        String username = "username";
        String password = "password";

        var user = new User("User", "username", passwordEncoder.encode(password));
        userRepository.save(user);

        var usernameToDelete = "todelete";
        var userToDelete = new User("To Delete", usernameToDelete, "password");
        userRepository.save(userToDelete);

        var expectedResponse = new DeletionResponse(usernameToDelete, "Deleted successfully!");

        var response = this.webClient
                .delete()
                .uri(DELETE_USER_ENDPOINT + "/" + usernameToDelete)
                .headers(headers -> headers.setBasicAuth(username, password))
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
}