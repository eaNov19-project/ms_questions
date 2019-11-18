package ea.sof.ms_questions.model;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionReqModel {

    @NotEmpty(message = "Please provide the title")
    private String title;

    @NotEmpty(message = "Please provide the body")
    private String body;
}
