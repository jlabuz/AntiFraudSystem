package antifraud.ip;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

public class IPRequest {
    @NotEmpty
    @Pattern(regexp = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$")
    private String ip;

    public IPRequest(String ip) {
        this.ip = ip;
    }

    public IPRequest() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
