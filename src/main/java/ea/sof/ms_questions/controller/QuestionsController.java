package ea.sof.ms_questions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.model.QuestionReqModel;
import ea.sof.ms_questions.repository.QuestionPaginationRepository;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.ms_questions.service.AuthServiceCircuitBreaker;
import ea.sof.shared.models.Question;
import ea.sof.shared.models.QuestionFollowers;
import ea.sof.shared.models.Response;
import ea.sof.shared.models.TokenUser;
import ea.sof.shared.utils.EaUtils;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/questions")
public class QuestionsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceCircuitBreaker.class);

    @Autowired
    private Environment env;

    @Value("${service.secret}")
    private String serviceSecret;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    QuestionPaginationRepository questionPaginationRepository;

    @Autowired
    AuthServiceCircuitBreaker authService;


    private Gson gson = new Gson();

    @Value("${APP_VERSION}")
    private String appVersion;

    @GetMapping("/health")
    public ResponseEntity<?> index() {
        String host = "Unknown host";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>("Questions service (" + appVersion + "). Host: " + host, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/")
    public ResponseEntity<?> getAllQuestions() {

        //TODO: implement server-side pagination
        //Returning all records for now
        List<QuestionEntity> storedQuestions = questionRepository.findAllByActiveEquals(1);
        List<Question> questions = storedQuestions.stream().map(qe -> qe.toQuestionModel()).collect(Collectors.toList());

        Response response = new Response(true, "");
        response.getData().put("questions", questions);

        return ResponseEntity.ok(response);
    }

	/*@CrossOrigin
	@GetMapping("/{page}")
	public ResponseEntity<?> getAllQuestionsPaginated(@PathVariable("page") Integer page) {

		Page<QuestionEntity> questionEntities = questionPaginationRepository
				.findAll(PageRequest.of(0, 10));

	questionEntities.getTotalElements();
		List<QuestionEntity> storedQuestions = questionRepository.findAll();
		List<Question> questions = storedQuestions.stream().map(qe -> qe.toQuestionModel()).collect(Collectors.toList());

		Response response = new Response(true, "");
		response.getData().put("questions", questions);

		return ResponseEntity.ok(response);
	}*/

    @CrossOrigin
    @GetMapping("/users/{uid}")
    public ResponseEntity<?> getAllQuestionsByUser(HttpServletRequest request) {
        LOGGER.info("Get All questions for user :: New request");

        //Check if request is authorized
        Response authCheckResp = isAuthorized(request.getHeader("Authorization"));
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authCheckResp);
        }

        ObjectMapper mapper = new ObjectMapper();
        TokenUser decodedToken = mapper.convertValue(authCheckResp.getData().get("decoded_token"), TokenUser.class);

        Response response = new Response();

        try {
            List<QuestionEntity> storedQuestions = questionRepository.findByUserId(decodedToken.getUserId().toString());

            List<Question> questions = storedQuestions.stream().map(qe -> qe.toQuestionModel()).collect(Collectors.toList());

            response.getData().put("questions", questions);
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            LOGGER.warn("Get all questions for user :: Exception. " + ex.getMessage());
        }

        return ResponseEntity.ok(response);


    }

    @CrossOrigin
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable("id") String id) {
        LOGGER.info("Get questions by id: " + id);

        QuestionEntity question = questionRepository.findById(id).orElse(null);
        if (question == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(false, "No match found"));
        }

        return ResponseEntity.ok(new Response(true, "question", question.toQuestionModel()));
    }


    //**************REQUIRES AUTHENTICATION**********************//

    @CrossOrigin
    @PostMapping("/")
    public ResponseEntity<?> createQuestion(@RequestBody(required = true) @Valid QuestionReqModel question, HttpServletRequest request) {
        LOGGER.info("CreateQuestion :: New request: " + question.toString());

        //Check if request is authorized
        Response authCheckResp = isAuthorized(request.getHeader("Authorization"));
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }

        QuestionEntity questionEntity = new QuestionEntity(question);
        //TokenUser decodedToken = (TokenUser) authCheckResp.getData().get("decoded_token");

        ObjectMapper mapper = new ObjectMapper();
        TokenUser decodedToken = mapper.convertValue(authCheckResp.getData().get("decoded_token"), TokenUser.class);
        questionEntity.setUserId(decodedToken.getUserId().toString());
        questionEntity.setUserEmail(decodedToken.getEmail());

        Response response = new Response();
        try {
            questionEntity = questionRepository.save(questionEntity);

            response.setMessage("Question has been created");
            response.addObject("question", questionEntity.toQuestionModel());

            kafkaTemplate.send(env.getProperty("topicNewQuestion"), gson.toJson(questionEntity.toQuestionQueueModel()));

            LOGGER.info("CreateQuestion :: Saved successfully. " + questionEntity.toString());
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            LOGGER.warn("CreateQuestion :: Error. " + ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @CrossOrigin
    @PatchMapping("/{questionId}/upvote")
    public ResponseEntity<?> upvote(@PathVariable("questionId") String questionId, HttpServletRequest request) {
        LOGGER.info("Upvote :: New request: " + questionId);

        //Check if request is authorized
        Response authCheckResp = isAuthorized(request.getHeader("Authorization"));
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }

        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if (questionEntity == null) {
            LOGGER.warn("Upvote :: Error. Question entity not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
        }

        Response response = new Response();
        try {
            questionEntity.upvote();
            questionEntity = questionRepository.save(questionEntity);

            response = new Response(true, "Question upvoted");
            response.addObject("question", questionEntity.toQuestionModel());

            LOGGER.info("Upvote :: Saved successfully. " + questionEntity.toString());
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            LOGGER.warn("Upvote :: Error. " + ex.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @CrossOrigin
    @PatchMapping("/{questionId}/downvote")
    public ResponseEntity<?> downvote(@PathVariable("questionId") String questionId, HttpServletRequest request) {
        LOGGER.info("Downvote :: New request: " + questionId);

        //Check if request is authorized
        Response authCheckResp = isAuthorized(request.getHeader("Authorization"));
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
        }

        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if (questionEntity == null) {
            LOGGER.warn("Downvote :: Error. Question entity not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
        }

        Response response = new Response();
        try {
            questionEntity.downvote();
            questionEntity = questionRepository.save(questionEntity);

            response = new Response(true, "Question downvoted");
            response.addObject("question", questionEntity.toQuestionModel());

            LOGGER.info("Upvote :: Saved successfully. " + questionEntity.toString());
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            LOGGER.warn("Upvote :: Error. " + ex.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @CrossOrigin
    @PostMapping("/{questionId}/follow")
    public ResponseEntity<?> follow(@PathVariable("questionId") String questionId, HttpServletRequest request) {
        LOGGER.info("Follow :: New request: " + questionId);

        //Check if request is authorized
        Response authCheckResp = isAuthorized(request.getHeader("Authorization"));
        if (!authCheckResp.getSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authCheckResp);
        }

        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if (questionEntity == null) {
            LOGGER.warn("Follow :: Error. Question entity not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
        }

        ObjectMapper mapper = new ObjectMapper();
        TokenUser decoded_token = mapper.convertValue(authCheckResp.getData().get("decoded_token"), TokenUser.class);
        String email = decoded_token.getEmail();
        LOGGER.info("Follow :: User email: " + email);
        Response response = new Response();

        try {
            questionEntity.addFollowerEmail(email);
            questionRepository.save(questionEntity);

            response = new Response(true, "Following the question");

            LOGGER.info("Follow :: Saved successfully. " + questionEntity.toString());
        } catch (Exception ex) {
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            LOGGER.warn("Follow :: Error. " + ex.getMessage());
        }

        return ResponseEntity.ok(response);
    }


    //******************ENDPOINTS FOR SERVICES*******************//

    @GetMapping("/{questionId}/followers")
    ResponseEntity<QuestionFollowers> getFollowersByQuestionId(@PathVariable("questionId") String questionId, HttpServletRequest request) {
        if (!EaUtils.isServiceAuthorized(request, serviceSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if (questionEntity == null) {
            System.out.println("Question Followers :: Error. Question entity not found");
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(questionEntity.toQuestionFollowersModel());
    }

    private Response isAuthorized(String authHeader) {
        LOGGER.info("JWT :: Checking authorization... ");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warn("Invalid token. Header null or 'Bearer ' is not provided.");
            return new Response(false, "Invalid token");
        }
        try {
            LOGGER.info("Calling authService.validateToken... ");
            ResponseEntity<Response> result = authService.validateToken(authHeader);

            LOGGER.info("AuthService replied... ");
            if (!result.getBody().getSuccess()) {
                LOGGER.warn("Filed to authorize. JWT is invalid");
                return result.getBody();
//				return new Response(false, "Invalid token");
            }

            LOGGER.info("Authorized successfully");
            return result.getBody();

        } catch (Exception e) {
            LOGGER.warn("Failed. " + e.getMessage());
            return new Response(false, "exception", e);
        }
    }
}
