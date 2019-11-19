package ea.sof.ms_questions.interfaces;

import ea.sof.shared.showcases.MsAuthShowcase;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="authService", url = "${AUTHENTICATE_SERVICE}")
//@FeignClient(name = "${feign.name}", url = "${feign.url}")
public interface AuthService extends MsAuthShowcase {

}
