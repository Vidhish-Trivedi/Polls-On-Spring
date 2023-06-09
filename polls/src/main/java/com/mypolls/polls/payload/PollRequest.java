package com.mypolls.polls.payload;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.mypolls.polls.model.ChoiceRequest;
import com.mypolls.polls.model.PollLength;

public class PollRequest {
    @NotBlank
    @Size(max = 256)
    private String question;

    @NotNull
    @Size(min = 2, max = 6)
    @Valid
    private List <ChoiceRequest> choices;

    @NotNull
    @Valid
    private PollLength pollLength;


    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<ChoiceRequest> getChoices() {
        return this.choices;
    }

    public void setChoices(List<ChoiceRequest> choices) {
        this.choices = choices;
    }

    public PollLength getPollLength() {
        return this.pollLength;
    }

    public void setPollLength(PollLength pollLength) {
        this.pollLength = pollLength;
    }
}
