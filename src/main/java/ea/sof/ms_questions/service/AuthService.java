package ea.sof.ms_questions.service;

import ea.sof.shared.models.Response;
import ea.sof.shared.showcases.MsAuthShowcase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "authms", url = "${AUTH:http://104.154.33.123:8080/auth}" )//)http://104.154.33.123:8080/auth")
public interface AuthService extends MsAuthShowcase {

}
