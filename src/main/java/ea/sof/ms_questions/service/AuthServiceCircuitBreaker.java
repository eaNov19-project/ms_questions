package ea.sof.ms_questions.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import ea.sof.ms_questions.interfaces.AuthService;
import ea.sof.shared.models.Response;
import jdk.internal.jline.internal.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

@Service
public class AuthServiceCircuitBreaker {
    @Autowired
    AuthService authService;

    @HystrixCommand(fallbackMethod = "fallback")
    public ResponseEntity<Response> validateToken(String token){
        return authService.validateToken(token);
    }

    public ResponseEntity<Response> fallback(String token) {
        Log.warn("AuthService is not available: validateToken fallback");
        return ResponseEntity.ok(new Response());
    }
}
