package antifraud.auth;

import antifraud.auth.dto.RegisterRequest;
import antifraud.auth.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class UserRegistrationTest extends UserControllerTest {
    private static final String REGISTER_USER_ENDPOINT = "/api/auth/user";

    @Test
    void shouldRegisterUserAndReturnUserDTOWithCreatedStatus() {
        RegisterRequest requestDTO = new RegisterRequest("John Doe", "johndoe", "secretpassword");
        var request = buildRegistrationRequest(requestDTO);
        var response = request.exchange();

        response
                .expectStatus().isCreated()
                .expectBody(UserDTO.class)
                .consumeWith(body -> {
                    UserDTO result = body.getResponseBody();
                    assertNotNull(result);
                    assertEquals(requestDTO.getUsername(), result.getUsername());
                    assertEquals(requestDTO.getName(), result.getName());
                });
        assertTrue(userRepository.findByUsername(requestDTO.getUsername()).isPresent());
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

    @Test
    void shouldReturnConflictForRegistrationOfExistingUsername() {
        RegisterRequest request = new RegisterRequest("Another user", ADMIN_USERNAME, "pass");

        var response = buildRegistrationRequest(request).exchange();

        response.expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }



    private WebTestClient.RequestHeadersSpec<?> buildRegistrationRequest(RegisterRequest request) {
        return this.webClient
                .post()
                .uri(REGISTER_USER_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request);
    }
}
