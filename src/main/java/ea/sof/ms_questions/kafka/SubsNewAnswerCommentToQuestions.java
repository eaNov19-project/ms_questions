package ea.sof.ms_questions.kafka;

import com.google.gson.Gson;
import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.ms_questions.service.AuthServiceCircuitBreaker;
import ea.sof.shared.entities.AnswerEntity;
import ea.sof.shared.entities.CommentAnswerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubsNewAnswerCommentToQuestions {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceCircuitBreaker.class);

    @Autowired
    QuestionRepository questionRepository;

    @KafkaListener(topics = "${topicNewAnswerComment}", groupId = "${subsNewAnswerCommentToQuestions}")
    public void newCommentAnswerEntity(String message) {

        LOGGER.info("SubsNewAnswerCommentToQuestions: New message from topic: " + message);

        Gson gson = new Gson();
        CommentAnswerEntity commentAnswerEntity =  gson.fromJson(message, CommentAnswerEntity.class);

        String answerId = commentAnswerEntity.getAnswerId();

        List<QuestionEntity> questionEntities = questionRepository.findAllByTopAnswers(answerId);


        for(QuestionEntity qe: questionEntities){
            for(AnswerEntity answerEntity: qe.getTopAnswers()) {
                if(answerEntity.getId().equals(answerId)) {
                    answerEntity.addAnswerComment(commentAnswerEntity);
                    questionRepository.save(qe);
                    LOGGER.info("Comment added to answer");
                }
            }
        }

    }
}
