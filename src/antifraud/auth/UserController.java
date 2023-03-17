package antifraud.auth;

import antifraud.auth.dto.RegisterRequest;
import antifraud.auth.dto.UserDTO;
import antifraud.auth.exceptions.UsernameAlreadyUsedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("user")
    public ResponseEntity<UserDTO> registerUser(@RequestBody @Valid RegisterRequest request) {
        try {
            User user = userService.createUser(request.getName(), request.getUsername(), request.getPassword());
            UserDTO response = new UserDTO(user.getId(), user.getName(), user.getUsername());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (UsernameAlreadyUsedException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @GetMapping("list")
    public List<UserDTO> listUsers() {
        List<User> users = userService.listUsers();
        return mapUsersToUserDTOs(users);
    }

    private static List<UserDTO> mapUsersToUserDTOs(List<User> users) {
        return users.stream()
                .map(UserDTO::mapUserToUserDTO)
                .collect(Collectors.toList());
    }
}
