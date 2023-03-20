package antifraud.auth.dto;

public class LockResponse {
    private final String status;

    public LockResponse(String username, boolean lock) {
        this.status = String.format("User %s %s!", username, lock ? "locked" : "unlocked");
    }

    public String getStatus() {
        return status;
    }
}
