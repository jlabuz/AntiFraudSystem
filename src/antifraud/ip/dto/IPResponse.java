package antifraud.ip.dto;

import antifraud.auth.User;
import antifraud.ip.IP;

public class IPResponse {
    private final long id;
    private final String ip;

    public IPResponse(long id, String ip) {
        this.id = id;
        this.ip = ip;
    }

    public long getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public static IPResponse mapIPToIPResponse(IP ip) {
        return new IPResponse(ip.getId(), ip.getIp());
    }
}
