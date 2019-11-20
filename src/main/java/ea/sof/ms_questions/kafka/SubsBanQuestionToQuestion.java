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
public class SubsBanQuestionToQuestion {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceCircuitBreaker.class);

	@Autowired
	QuestionRepository questionRepository;

	@KafkaListener(topics = "${topicBanQuestion}", groupId = "${subsBanQuestionToQuestion}")
	public void listener(String questionId) {
		LOGGER.info("SubsBanQuestionToQuestion :: New message from topic 'topicBanQuestion': " + questionId);

		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if (questionEntity == null) {
			LOGGER.warn("SubsBanQuestionToQuestion :: Failed to retrieve Entity.");
			return;
		}

		try {
			questionEntity.setActive(0);
			questionRepository.save(questionEntity);
			LOGGER.info("SubsBanQuestionToQuestion :: Question banned");
		} catch (Exception ex){
			LOGGER.error("SubsBanQuestionToQuestion :: Failed to save Entity: " + ex.getMessage());
		}
	}

}
