package ea.sof.ms_questions.repository;

import ea.sof.ms_questions.entity.QuestionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuestionRepository extends MongoRepository<QuestionEntity, Long> {
}
