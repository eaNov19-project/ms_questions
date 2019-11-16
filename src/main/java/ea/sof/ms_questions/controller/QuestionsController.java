package ea.sof.ms_questions.controller;

import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.model.QuestionReqModel;
import ea.sof.ms_questions.pubsub.PubSubQuestionSender;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.shared.models.Question;
import ea.sof.shared.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
public class QuestionsController {

    @Autowired
    QuestionRepository questionRepository;

//    @Autowired
//    PubSubQuestionSender.PubsubOutboundQuestionsGateway questionsSender;

    @GetMapping
    public ResponseEntity<?> getAllQuestions(){
        List<QuestionEntity> storedQuestions = questionRepository.findAll();
        List<Question> questions = storedQuestions.stream().map(qe -> qe.toQuestionModel()).collect(Collectors.toList());

        Response response = new Response(true, "");
        response.getData().put("questions", questions);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable("id") String id){

        QuestionEntity question = questionRepository.findById(id).orElse(null);
        if(question == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(false, "No match found"));
        }

        return ResponseEntity.ok(new Response(true, "question", question));
    }

    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody(required = true) @Valid QuestionReqModel question){
        QuestionEntity questionEntity = new QuestionEntity(question);

        Response response = new Response(true, "Question has been created");
        questionEntity = questionRepository.save(questionEntity);
        response.addObject("question", questionEntity);

//        questionsSender.sendToPubsub(new JSONObject(questionEntity).toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PatchMapping("/{questionId}/upvote")
    public ResponseEntity<?> upvote(@PathVariable("questionId") String questionId, Model model){
//        model.getAttribute("tokendata");
        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if(questionEntity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
        }

        questionEntity.upvote();
        questionEntity = questionRepository.save(questionEntity);

        Response response = new Response(true, "Question upvoted");
        response.addObject("question", questionEntity);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{questionId}/downvote")
    public ResponseEntity<?> downvote(@PathVariable("questionId") String questionId){
        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if(questionEntity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
        }

        questionEntity.downvote();
        questionEntity = questionRepository.save(questionEntity);

        Response response = new Response(true, "Question downvoted");
        response.getData().put("question", questionEntity);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{questionId}/follow")
    public ResponseEntity<?> follow(@PathVariable("questionId") String questionId){
        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
        if(questionEntity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(false, "No match found"));
        }

        //TODO: get email from token
        String email = "";

        questionEntity.addFollowerEmail(email);
        questionEntity = questionRepository.save(questionEntity);

        Response response = new Response(true, "Folowing the question");
        return ResponseEntity.ok(response);
    }

}
