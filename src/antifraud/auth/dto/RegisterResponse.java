package antifraud.auth.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class RegisterResponse {
    @NotNull
    private Long id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String username;

    public RegisterResponse() {
    }

    public RegisterResponse(Long id, String name, String username) {
        this.id = id;
        this.name = name;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
}
