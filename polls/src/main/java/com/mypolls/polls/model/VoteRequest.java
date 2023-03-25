package com.mypolls.polls.model;

import javax.validation.constraints.NotNull;

public class VoteRequest {
    @NotNull
    private Long choiceId;

    public Long getChoiceId() {
        return(this.choiceId);
    }

    public void setChoiceid(Long choiceId) {
        this.choiceId = choiceId;
    }
}
