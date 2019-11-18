package ea.sof.ms_questions.kafka;

import com.google.gson.Gson;
import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.shared.entities.AnswerEntity;
import ea.sof.shared.entities.CommentAnswerEntity;
import ea.sof.shared.entities.CommentQuestionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubsNewAnswerCommentToQuestions {
    @Autowired
    QuestionRepository questionRepository;

    @KafkaListener(topics = "${topicNewAnswerComment}", groupId = "${subsNewAnswerCommentToQuestions}")
    public void newCommentAnswerEntity(String message) {

        System.out.println("SubsNewAnswerCommentToQuestions: New message from topic: " + message);

        Gson gson = new Gson();
        CommentAnswerEntity commentAnswerEntity =  gson.fromJson(message, CommentAnswerEntity.class);

        String answerId = commentAnswerEntity.getAnswerId();

        String questionId = commentAnswerEntity.getQuestionId();

        if (questionId == null) return;
        QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);

        if (questionEntity == null) return;

        List<AnswerEntity> answerEntityList = questionEntity.getTopAnswers();
        for(AnswerEntity answerEntity: answerEntityList) {
            if(answerEntity.getId().equals(answerId)) {
                answerEntity.addAnswerComment(commentAnswerEntity);
                questionRepository.save(questionEntity);
                System.out.println("Comment added to answer");
            }
        }
    }
}
