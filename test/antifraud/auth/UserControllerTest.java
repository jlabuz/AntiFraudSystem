package antifraud.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    protected static final String ADMIN_USERNAME = "admin";
    protected static final String MERCHANT_USERNAME = "merchant";
    protected static final String SUPPORT_USERNAME = "support";
    protected static final String PASSWORD = "password";

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected WebTestClient webClient;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.save(new User("Merchant", MERCHANT_USERNAME, passwordEncoder.encode(PASSWORD), Role.MERCHANT));
        userRepository.save(new User("Admin", ADMIN_USERNAME, passwordEncoder.encode(PASSWORD), Role.ADMINISTRATOR));
        userRepository.save(new User("Support", SUPPORT_USERNAME, passwordEncoder.encode(PASSWORD), Role.SUPPORT));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }
}