package ea.sof.ms_questions.kafka;

import com.google.gson.Gson;
import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.shared.entities.CommentQuestionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SubsNewQuestionCommentToQuestions {

	@Autowired
	QuestionRepository questionRepository;

	@KafkaListener(topics = "${topicNewQuestionComment}", groupId = "${subsNewQuestionCommentToQuestions}")
	public void newCommentQuestionEntity(String message) {

		System.out.println("SubsNewQuestionCommentToQuestions: New message from topic: " + message);

		Gson gson = new Gson();
		CommentQuestionEntity commentQuestionEntity =  gson.fromJson(message, CommentQuestionEntity.class);

		String questionId = commentQuestionEntity.getQuestionId();

		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if(questionEntity != null){
			questionEntity.addQuestionComment(commentQuestionEntity);
			questionRepository.save(questionEntity);
			System.out.println("Comment added");
		}
	}

}
