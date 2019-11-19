package ea.sof.ms_questions.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import ea.sof.ms_questions.controller.QuestionsController;
import ea.sof.ms_questions.interfaces.AuthService;
import ea.sof.shared.models.Response;
import jdk.internal.jline.internal.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

@Service
public class AuthServiceCircuitBreaker {
    @Autowired
    AuthService authService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceCircuitBreaker.class);

    @HystrixCommand(fallbackMethod = "fallback")
    public ResponseEntity<Response> validateToken(String token){
        return authService.validateToken(token);
    }

    public ResponseEntity<Response> fallback(String token) {
        LOGGER.warn("AuthService is not available: validateToken fallback");
        return ResponseEntity.ok(new Response(false, "Authentication service is unavailable. Try later"));
    }
}
