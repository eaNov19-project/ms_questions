package ea.sof.ms_questions.repository;

import ea.sof.ms_questions.entity.QuestionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface QuestionRepository extends MongoRepository<QuestionEntity, String> {
    Optional<QuestionEntity> findById(String id);
}
