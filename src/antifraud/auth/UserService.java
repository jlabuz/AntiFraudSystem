package antifraud.auth;

import antifraud.auth.exceptions.RoleAlreadyAssignedException;
import antifraud.auth.exceptions.UsernameAlreadyUsedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private static final String USERNAME_NOT_FOUND = "Username not found";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("No user with name %s", username)));
        return mapUserToUserDetails(user);

    }

    private static UserDetails mapUserToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    @Transactional
    public User createUser(String name, String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyUsedException(String.format("Username %s is already used", username));
        }

        boolean firstUser = userRepository.count() == 0;
        Role role = firstUser ? Role.ADMINISTRATOR : Role.MERCHANT;
        boolean locked = !firstUser;
        return userRepository.save(new User(name, username, passwordEncoder.encode(password), role, locked));
    }

    public List<User> listUsers() {
        return (List<User>) userRepository.findAll();
    }

    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USERNAME_NOT_FOUND));

        userRepository.delete(user);
    }

    @Transactional
    public User changeUserRole(String username, String roleName) {
        Role role = Role.valueOf(roleName);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USERNAME_NOT_FOUND));
        validateRoleChange(role, user);
        user.setRole(role);
        return userRepository.save(user);
    }

    private static void validateRoleChange(Role role, User user) {
        if (role == Role.ADMINISTRATOR) {
            throw new IllegalArgumentException();
        }
        if (role.equals(user.getRole())) {
            throw new RoleAlreadyAssignedException();
        }
    }

    public void changeUserLock(String username, boolean lock) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USERNAME_NOT_FOUND));
        validateLockChange(lock, user);
        user.setLocked(lock);
        userRepository.save(user);
    }

    private static void validateLockChange(boolean lock, User user) {
        if (user.getRole() == Role.ADMINISTRATOR && lock) {
            throw new IllegalArgumentException();
        }
    }
}
