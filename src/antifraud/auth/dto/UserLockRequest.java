package antifraud.auth.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class UserLockRequest {

    @NotEmpty
    private String username;

    @NotNull
    private Operation operation;

    public UserLockRequest(String username, Operation role) {
        this.username = username;
        this.operation = role;
    }

    public UserLockRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}

