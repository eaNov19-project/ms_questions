package ea.sof.ms_questions.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "questions")
public class QuestionEntity {

    @Transient
    public static final String SEQUENCE_NAME = "questions_sequence";

    @Id
    private long id;
    private String title;
}
