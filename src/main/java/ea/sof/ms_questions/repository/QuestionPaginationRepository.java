package ea.sof.ms_questions.repository;

import ea.sof.ms_questions.entity.QuestionEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface QuestionPaginationRepository extends PagingAndSortingRepository<QuestionEntity, String> {
}
