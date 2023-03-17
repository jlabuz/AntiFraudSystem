package antifraud.auth;

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
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    private static final String USER_ENDPOINT = "/api/auth/user";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebTestClient webClient;

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
}