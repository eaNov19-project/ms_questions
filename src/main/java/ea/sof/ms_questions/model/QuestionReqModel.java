package ea.sof.ms_questions.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionReqModel {

    @NotEmpty(message = "Please provide the title")
    private String title;

    @NotEmpty(message = "Please provide the body")
    private String body;
}
