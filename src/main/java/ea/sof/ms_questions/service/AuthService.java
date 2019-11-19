package ea.sof.ms_questions.service;

import ea.sof.shared.showcases.MsAuthShowcase;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="authService", url = "${authenticate.service}")
//@FeignClient(name = "${feign.name}", url = "${feign.url}")
public interface AuthService extends MsAuthShowcase {

}
