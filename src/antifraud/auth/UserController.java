package antifraud.auth;

import antifraud.auth.dto.RegisterRequest;
import antifraud.auth.dto.RegisterResponse;
import antifraud.auth.exceptions.UsernameAlreadyUsedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("user")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(request.getName(), request.getUsername(), request.getPassword());
            RegisterResponse response = new RegisterResponse(user.getId(), user.getName(), user.getUsername());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (UsernameAlreadyUsedException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
}
