package antifraud.auth.dto;

import javax.validation.constraints.NotEmpty;

public class RegisterRequest {
    @NotEmpty
    private String name;

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    public RegisterRequest(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public RegisterRequest() {
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
}
