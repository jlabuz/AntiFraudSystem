package antifraud.ip;

import org.springframework.data.annotation.Id;

import java.util.Objects;

public class IP {
    @Id
    private long id;
    private String ip;

    public IP(String ip) {
        this.ip = ip;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IP ip1 = (IP) o;
        return id == ip1.id && ip.equals(ip1.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ip);
    }
}
