package antifraud.ip;

import antifraud.ip.dto.IPResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/antifraud/suspicious-ip")
public class IPController {

    private IPService ipService;

    public IPController(IPService ipService) {
        this.ipService = ipService;
    }

    @GetMapping
    public void getSuspiciousIPs() {

    }

    @PostMapping
    public ResponseEntity<IPResponse> addSuspiciousIP(@RequestBody @Valid IPRequest request) {
        try {
            IP ip = ipService.addSuspiciousIP(request.getIp());
            return ResponseEntity.ok(IPResponse.mapIPToIPResponse(ip));
        } catch (IPAlreadyInDatabase ipAlreadyInDatabase) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping("/{ip}")
    public void deleteSuspiciousIP(@PathVariable String ip) {

    }
}
