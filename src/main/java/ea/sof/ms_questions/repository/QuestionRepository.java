package ea.sof.ms_questions.repository;

import ea.sof.ms_questions.entity.QuestionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends MongoRepository<QuestionEntity, String> {
    Optional<QuestionEntity> findById(String id);
    List<QuestionEntity> findByUserId(String id);
    List<QuestionEntity> findAllByActiveEqualsOrderByIdDesc(Integer active);

    @Query("{ 'topAnswers.id' : ?0 }")
    List<QuestionEntity> findAllByTopAnswers(String answerId);


}
