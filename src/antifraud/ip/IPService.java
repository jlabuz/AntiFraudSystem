package antifraud.ip;

import org.springframework.stereotype.Service;

@Service
public class IPService {
    private IPRepository ipRepository;

    public IPService(IPRepository ipRepository) {
        this.ipRepository = ipRepository;
    }

    public IP addSuspiciousIP(String ip) {
        if (ipRepository.findByIp(ip).isPresent()) {
            throw new IPAlreadyInDatabase();
        }
        return ipRepository.save(new IP(ip));
    }
}
