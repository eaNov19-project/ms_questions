package ea.sof.ms_questions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.model.QuestionReqModel;
import ea.sof.ms_questions.repository.QuestionPaginationRepository;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.ms_questions.service.AuthService;
import ea.sof.shared.models.Question;
import ea.sof.shared.models.QuestionFollowers;
import ea.sof.shared.models.Response;
import ea.sof.shared.models.TokenUser;
import ea.sof.shared.utils.EaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/questions")
public class QuestionsController {

	@Autowired
	private Environment env;

	@Value("service.secret")
	private String serviceSecret;

	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	QuestionRepository questionRepository;

	@Autowired
	QuestionPaginationRepository questionPaginationRepository;

	@Autowired
	AuthService authService;

	private Gson gson = new Gson();


	@CrossOrigin
	@GetMapping("/")
	public ResponseEntity<?> getAllQuestions() {

		List<QuestionEntity> storedQuestions = questionRepository.findAll();
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
	public ResponseEntity<?> getAllQuestionsByUser() {
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
	public ResponseEntity<?> createQuestion(@RequestBody(required = true) @Valid QuestionReqModel question, HttpServletRequest request) {
		System.out.println("CreateQuestion :: New request: " + question.toString());

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

		Response response = new Response();
		try {
			questionEntity = questionRepository.save(questionEntity);

			response.setMessage("Question has been created");
			response.addObject("question", questionEntity.toQuestionModel());

			//
			kafkaTemplate.send(env.getProperty("topicNewQuestion"), gson.toJson(questionEntity.toQuestionQueueModel()));

			System.out.println("CreateQuestion :: Saved successfully. " + questionEntity.toString());
		} catch (Exception ex) {
			response.setSuccess(false);
			response.setMessage(ex.getMessage());
			System.out.println("CreateQuestion :: Error. " + ex.getMessage());
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}


	@CrossOrigin
	@PatchMapping("/{questionId}/upvote")
	public ResponseEntity<?> upvote(@PathVariable("questionId") String questionId, HttpServletRequest request) {
		System.out.println("\nUpvote :: New request: " + questionId);

		//Check if request is authorized
		Response authCheckResp = isAuthorized(request.getHeader("Authorization"));
		if (!authCheckResp.getSuccess()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
		}

		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if (questionEntity == null) {
			System.out.println("Upvote :: Error. Question entity not found");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
		}

		Response response = new Response();
		try {
			questionEntity.upvote();
			questionEntity = questionRepository.save(questionEntity);

			response = new Response(true, "Question upvoted");
			response.addObject("question", questionEntity.toQuestionModel());

			System.out.println("Upvote :: Saved successfully. " + questionEntity.toString());
		} catch (Exception ex) {
			response.setSuccess(false);
			response.setMessage(ex.getMessage());
			System.out.println("Upvote :: Error. " + ex.getMessage());
		}

		return ResponseEntity.ok(response);
	}

	@CrossOrigin
	@PatchMapping("/{questionId}/downvote")
	public ResponseEntity<?> downvote(@PathVariable("questionId") String questionId, HttpServletRequest request) {
		System.out.println("\nDownvote :: New request: " + questionId);

		//Check if request is authorized
		Response authCheckResp = isAuthorized(request.getHeader("Authorization"));
		if (!authCheckResp.getSuccess()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
		}

		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if (questionEntity == null) {
			System.out.println("Downvote :: Error. Question entity not found");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
		}

		Response response = new Response();
		try {
			questionEntity.downvote();
			questionEntity = questionRepository.save(questionEntity);

			response = new Response(true, "Question downvoted");
			response.addObject("question", questionEntity.toQuestionModel());

			System.out.println("Upvote :: Saved successfully. " + questionEntity.toString());
		} catch (Exception ex) {
			response.setSuccess(false);
			response.setMessage(ex.getMessage());
			System.out.println("Upvote :: Error. " + ex.getMessage());
		}

		return ResponseEntity.ok(response);
	}

	@CrossOrigin
	@PostMapping("/{questionId}/follow")
	public ResponseEntity<?> follow(@PathVariable("questionId") String questionId, HttpServletRequest request) {
		System.out.println("\nFollow :: New request: " + questionId);

		//Check if request is authorized
		Response authCheckResp = isAuthorized(request.getHeader("Authorization"));
		if (!authCheckResp.getSuccess()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(false, "Invalid Token"));
		}

		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if (questionEntity == null) {
			System.out.println("Follow :: Error. Question entity not found");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
		}

        ObjectMapper mapper = new ObjectMapper();
        TokenUser decoded_token = mapper.convertValue(authCheckResp.getData().get("decoded_token"), TokenUser.class);
        String email = decoded_token.getEmail();
        System.out.println("Follow :: User email: " + email);
        Response response = new Response();

		try {
			questionEntity.addFollowerEmail(email);
			questionRepository.save(questionEntity);

			response = new Response(true, "Following the question");

            System.out.println("Follow :: Saved successfully. " + questionEntity.toString());
        } catch (Exception ex) {
			response.setSuccess(false);
			response.setMessage(ex.getMessage());
			System.out.println("Follow :: Error. " + ex.getMessage());
		}

		return ResponseEntity.ok(response);
	}


	//******************ENDPOINTS FOR SERVICES*******************//
	@GetMapping("/{questionId}/followers")
	ResponseEntity<QuestionFollowers> getFollowersByQuestionId(@PathVariable("questionId") String questionId, HttpServletRequest request){
		if(!EaUtils.isServiceAuthorized(request, serviceSecret)){
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if(questionEntity == null){
			System.out.println("Question Followers :: Error. Question entity not found");
			return ResponseEntity.badRequest().build();
		}

		return ResponseEntity.ok(questionEntity.toQuestionFollowersModel());
	}

	private Response isAuthorized(String authHeader) {
		System.out.print("JWT :: Checking authorization... ");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			System.out.println("Invalid token. Header null or 'Bearer ' is not provided.");
			return new Response(false, "Invalid token");
		}
		try {
			System.out.print("Calling authService.validateToken... ");
			ResponseEntity<Response> result = authService.validateToken(authHeader);

			System.out.print("AuthService replied... ");
			if (!result.getBody().getSuccess()) {
				System.out.println("Filed to authorize. JWT is invalid");
				return new Response(false, "Invalid token");
			}

			System.out.println("Authorized successfully");
			return result.getBody();

		} catch (Exception e) {
			System.out.println("Failed. " + e.getMessage());
			return new Response(false, "exception", e);
		}
	}
}
