package ea.sof.ms_questions.kafka;

import com.google.gson.Gson;
import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.ms_questions.service.AuthServiceCircuitBreaker;
import ea.sof.shared.entities.CommentQuestionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SubsNewQuestionCommentToQuestions {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceCircuitBreaker.class);

	@Autowired
	QuestionRepository questionRepository;

	@KafkaListener(topics = "${topicNewQuestionComment}", groupId = "${subsNewQuestionCommentToQuestions}")
	public void listener(String message) {
		LOGGER.info("SubsNewQuestionCommentToQuestions :: New message from topic 'topicNewQuestionComment': " + message);

		CommentQuestionEntity commentQuestionEntity = null;
		try {
			Gson gson = new Gson();
			commentQuestionEntity = gson.fromJson(message, CommentQuestionEntity.class);
		} catch (Exception ex) {
			LOGGER.error("SubsNewQuestionCommentToQuestions :: Failed to convert Json: " + ex.getMessage());
		}

		String questionId = commentQuestionEntity.getQuestionId();

		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if (questionEntity == null) {
			LOGGER.warn("SubsNewQuestionCommentToQuestions :: Failed to retrieve Entity.");
			return;
		}

		try {
			questionEntity.addQuestionComment(commentQuestionEntity);
			questionRepository.save(questionEntity);
			LOGGER.info("SubsNewQuestionCommentToQuestions :: Comment added");
		} catch (Exception ex){
			LOGGER.error("SubsNewQuestionCommentToQuestions :: Failed to save Entity: " + ex.getMessage());
		}
	}

}
