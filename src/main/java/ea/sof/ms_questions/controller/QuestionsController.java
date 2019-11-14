package ea.sof.ms_questions.controller;

import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.ms_questions.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionsController {

    @Autowired
    SequenceGeneratorService sequenceGenerator;

    @Autowired
    QuestionRepository questionRepository;

    @GetMapping
    public String getAllQuestions(){
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setId(sequenceGenerator.generateSequence(QuestionEntity.SEQUENCE_NAME));
        questionEntity.setTitle("john.doe@example.com");
        questionRepository.save(questionEntity);


        List<QuestionEntity> storedQuestions = questionRepository.findAll();
        storedQuestions.forEach(System.out::println);
        return "All questions";
    }

}
