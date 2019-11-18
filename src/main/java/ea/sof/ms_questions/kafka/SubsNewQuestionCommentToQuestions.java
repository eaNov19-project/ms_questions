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
	public void listener(String message) {
		System.out.println("\nSubsNewQuestionCommentToQuestions :: New message from topic 'topicNewQuestionComment': " + message);

		CommentQuestionEntity commentQuestionEntity = null;
		try {
			Gson gson = new Gson();
			commentQuestionEntity = gson.fromJson(message, CommentQuestionEntity.class);
		} catch (Exception ex) {
			System.out.println("SubsNewQuestionCommentToQuestions :: Failed to convert Json: " + ex.getMessage());
		}

		String questionId = commentQuestionEntity.getQuestionId();

		QuestionEntity questionEntity = questionRepository.findById(questionId).orElse(null);
		if (questionEntity == null) {
			System.out.println("SubsNewQuestionCommentToQuestions :: Failed to retrieve Entity.");
			return;
		}

		try {
			questionEntity.addQuestionComment(commentQuestionEntity);
			questionRepository.save(questionEntity);
			System.out.println("SubsNewQuestionCommentToQuestions :: Comment added");
		} catch (Exception ex){
			System.out.println("SubsNewQuestionCommentToQuestions :: Failed to save Entity: " + ex.getMessage());
		}
	}

}
