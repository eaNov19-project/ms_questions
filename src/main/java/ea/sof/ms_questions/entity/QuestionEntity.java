package ea.sof.ms_questions.entity;

import ea.sof.ms_questions.model.QuestionReqModel;
import ea.sof.shared.entities.AnswerEntity;
import ea.sof.shared.entities.CommentQuestionEntity;
import ea.sof.shared.models.Question;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Document(collection = "questions")
public class QuestionEntity {

    @Id
    private String id;
    private String userId;
    private String title;
    private String body;
    private LocalDateTime created;
    private LocalDateTime lastEdited;
    private Integer votes = 0;
    private List<CommentQuestionEntity> topComments = new ArrayList<>();
    private List<AnswerEntity> topAnswers = new ArrayList<>();

    private List<String> followers = new ArrayList<>();

    public QuestionEntity(QuestionReqModel questionModel) {
        this.title = questionModel.getTitle();
        this.body = questionModel.getBody();
        this.created = LocalDateTime.now();
        this.lastEdited = this.created;

        //TODO: Add user id from logged in user token
    }

    public Question toQuestionModel(){
        Question questionModel = new Question();
        questionModel.setId(this.id);
        questionModel.setUserId(this.userId);
        questionModel.setTitle(this.title);
        questionModel.setBody(this.body);
        questionModel.setCreated(this.created);
        questionModel.setLastEdited(this.lastEdited);
        questionModel.setUpvotes(this.votes);

        return questionModel;
    }

    public void upvote(){
        this.votes++;
    }
    public void downvote(){
        this.votes--;
    }

}
