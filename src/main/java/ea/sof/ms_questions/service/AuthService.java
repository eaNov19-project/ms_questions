package ea.sof.ms_questions.service;

import ea.sof.shared.models.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "svc-bank", url = "http://localhost:8888")
public interface AuthService {

    @PostMapping("/validate-token")
    Response isValidToken(@RequestHeader("Bearer Token") String jwt);

}
