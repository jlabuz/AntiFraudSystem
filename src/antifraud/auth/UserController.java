package antifraud.auth;

import antifraud.auth.dto.*;
import antifraud.auth.exceptions.RoleAlreadyAssignedException;
import antifraud.auth.exceptions.UsernameAlreadyUsedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
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
            UserDTO response = UserDTO.mapUserToUserDTO(user);
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

    @DeleteMapping("user/{username}")
    public ResponseEntity<DeletionResponse> deleteUser(@PathVariable String username) {
        try {
            userService.deleteUser(username);
            return ResponseEntity.ok(new DeletionResponse(username, "Deleted successfully!"));
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("role")
    public ResponseEntity<UserDTO> changeUserRole(@RequestBody @Valid RoleChangeRequest request) {
        try {
            User user = userService.changeUserRole(request.getUsername(), request.getRole());
            return ResponseEntity.ok(UserDTO.mapUserToUserDTO(user));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RoleAlreadyAssignedException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping("access")
    public ResponseEntity<LockResponse> changeUserLock(@RequestBody @Valid UserLockRequest request) {
        try {
            boolean lock = request.getOperation() == Operation.LOCK;
            userService.changeUserLock(request.getUsername(), lock);
            return ResponseEntity.ok(new LockResponse(request.getUsername(), lock));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
