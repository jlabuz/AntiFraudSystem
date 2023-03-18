package antifraud.auth.dto;

import java.util.Objects;

public class DeletionResponse {
    private final String username;
    private final String status;

    public DeletionResponse(String username, String status) {
        this.username = username;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletionResponse that = (DeletionResponse) o;
        return username.equals(that.username) && status.equals(that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, status);
    }

    @Override
    public String toString() {
        return "DeletionResponse{" +
               "username='" + username + '\'' +
               ", status='" + status + '\'' +
               '}';
    }
}
