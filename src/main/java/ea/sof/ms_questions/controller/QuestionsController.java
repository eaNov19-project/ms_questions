package ea.sof.ms_questions.controller;

import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.model.QuestionReqModel;
import ea.sof.ms_questions.pubsub.PubSubQuestionSender;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.ms_questions.service.AuthService;
import ea.sof.shared.models.Question;
import ea.sof.shared.models.Response;
import ea.sof.shared.models.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/questions")
public class QuestionsController {

    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    AuthService authService;

//    @Autowired
//    PubSubQuestionSender.PubsubOutboundQuestionsGateway questionsSender;

    @CrossOrigin
    @GetMapping
    public ResponseEntity<?> getAllQuestions() {
        List<QuestionEntity> storedQuestions = questionRepository.findAll();
        List<Question> questions = storedQuestions.stream().map(qe -> qe.toQuestionModel()).collect(Collectors.toList());

        Response response = new Response(true, "");
        response.getData().put("questions", questions);

        return ResponseEntity.ok(response);
    }

    @CrossOrigin
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable("id") String id) {

        QuestionEntity question = questionRepository.findById(id).orElse(null);
        if (question == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(false, "No match found"));
        }

        return ResponseEntity.ok(new Response(true, "question", question.toQuestionModel()));
    }


    //**************REQUIRES AUTHENTICATION**********************//

    @CrossOrigin
    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody(required = true) @Valid QuestionReqModel question, @RequestHeader("Authorization") String token) {

        //Check if request is authorized
        Response authCheckResp = isAuthorized(token);
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }

        QuestionEntity questionEntity = new QuestionEntity(question);

        Response response = new Response(true, "Question has been created");
        questionEntity = questionRepository.save(questionEntity);
        response.addObject("question", questionEntity.toQuestionModel());

//        questionsSender.sendToPubsub(new JSONObject(questionEntity).toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @CrossOrigin
    @PatchMapping("/{questionId}/upvote")
    public ResponseEntity<?> upvote(@PathVariable("questionId") String questionId, @RequestHeader("Authorization") String token) {

        //Check if request is authorized
        Response authCheckResp = isAuthorized(token);
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }

        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if (questionEntity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
        }

        questionEntity.upvote();
        questionEntity = questionRepository.save(questionEntity);

        Response response = new Response(true, "Question upvoted");
        response.addObject("question", questionEntity.toQuestionModel());

        return ResponseEntity.ok(response);
    }

    @CrossOrigin
    @PatchMapping("/{questionId}/downvote")
    public ResponseEntity<?> downvote(@PathVariable("questionId") String questionId, @RequestHeader("Authorization") String token) {

        //Check if request is authorized
        Response authCheckResp = isAuthorized(token);
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }

        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if (questionEntity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
        }

        questionEntity.downvote();
        questionEntity = questionRepository.save(questionEntity);

        Response response = new Response(true, "Question downvoted");
        response.getData().put("question", questionEntity.toQuestionModel());

        return ResponseEntity.ok(response);
    }

    @CrossOrigin
    @PostMapping("/{questionId}/follow")
    public ResponseEntity<?> follow(@PathVariable("questionId") String questionId, @RequestHeader("Authorization") String token) {

        //Check if request is authorized
        Response authCheckResp = isAuthorized(token);
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }

        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if (questionEntity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
        }

        TokenUser decoded_token = (TokenUser) authCheckResp.getData().get("decoded_token");
        String email = decoded_token.getEmail();
        System.out.println(email);

        questionEntity.addFollowerEmail(email);
        questionRepository.save(questionEntity);

        Response response = new Response(true, "Folowing the question");
        return ResponseEntity.ok(response);
    }


    private Response isAuthorized(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Response(false, "Invalid token");
        }
        try {
            ResponseEntity<Response> result = authService.validateToken(authHeader);

            if (!result.getBody().getSuccess()) {
                return new Response(false, "Invalid token");
            }
            return result.getBody();

        }catch (Exception e){
            return new Response(false, "exception", e);
        }
    }
}
