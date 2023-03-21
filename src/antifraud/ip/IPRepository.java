package antifraud.ip;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface IPRepository extends CrudRepository<IP, Long> {
    Optional<IP> findByIp(String ip);
}
