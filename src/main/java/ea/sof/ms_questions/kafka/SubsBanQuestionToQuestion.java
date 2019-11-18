package ea.sof.ms_questions.kafka;

import com.google.gson.Gson;
import ea.sof.ms_questions.entity.QuestionEntity;
import ea.sof.ms_questions.repository.QuestionRepository;
import ea.sof.shared.entities.CommentQuestionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SubsBanQuestionToQuestion {

	@Autowired
	QuestionRepository questionRepository;

	@KafkaListener(topics = "${topicBanQuestion}", groupId = "${subsBanQuestionToQuestion}")
	public void listener(String questionId) {
		System.out.println("\nSubsBanQuestionToQuestion :: New message from topic 'topicBanQuestion': " + questionId);

		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if (questionEntity == null) {
			System.out.println("SubsBanQuestionToQuestion :: Failed to retrieve Entity.");
			return;
		}

		try {
			questionEntity.setActive(0);
			questionRepository.save(questionEntity);
			System.out.println("SubsBanQuestionToQuestion :: Question banned");
		} catch (Exception ex){
			System.out.println("SubsBanQuestionToQuestion :: Failed to save Entity: " + ex.getMessage());
		}
	}

}
