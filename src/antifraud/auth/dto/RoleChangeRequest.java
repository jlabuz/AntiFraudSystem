package antifraud.auth.dto;

import javax.validation.constraints.NotEmpty;

public class RoleChangeRequest {
    @NotEmpty
    private String username;

    @NotEmpty
    private String role;

    public RoleChangeRequest(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public RoleChangeRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
