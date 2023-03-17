package antifraud.auth;

import antifraud.auth.exceptions.UsernameAlreadyUsedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

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
                .roles("USER")
                .build();
    }

    public User createUser(String name, String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyUsedException(String.format("Username %s is already used", username));
        }

        return userRepository.save(new User(name, username, passwordEncoder.encode(password)));
    }

    public List<User> listUsers() {
        return (List<User>) userRepository.findAll();
    }
}
