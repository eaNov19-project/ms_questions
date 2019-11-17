package ea.sof.ms_questions.service;

import ea.sof.shared.models.Response;
import ea.sof.shared.showcases.MsAuthShowcase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name="authService", url = "${AUTHENTICATE_SERVICE}")
//@FeignClient(name = "${feign.name}", url = "${feign.url}")
public interface AuthService extends MsAuthShowcase {

}
