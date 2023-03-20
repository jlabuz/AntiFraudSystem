package antifraud.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name="USERS")
public class User {
    @Id
    private long id;
    private String name;
    private String username;
    private String password;
    private Role role;
    private boolean locked;

    public User() {
    }

    public User(String name, String username, String password, Role role, boolean locked) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.locked = locked;
    }

    public User(String name, String username, String password, Role role) {
        this(name, username, password, role, false);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", username='" + username + '\'' +
               ", password='" + password + '\'' +
               ", role='" + role + '\'' +
               ", locked='" + locked + '\'' +
               '}';
    }
}
