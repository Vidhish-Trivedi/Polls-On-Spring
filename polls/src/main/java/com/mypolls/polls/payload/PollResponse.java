package com.mypolls.polls.payload;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PollResponse {
    private Long id;
    private String question;
    private List <ChoiceResponse> choices;
    private UserSummary createdBy;
    private Instant creationDateTime;
    private Instant expirationDateTime;
    private Boolean isExpired;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long selectedChoice;
    private Long totalVotes;


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<ChoiceResponse> getChoices() {
        return this.choices;
    }

    public void setChoices(List<ChoiceResponse> choices) {
        this.choices = choices;
    }

    public UserSummary getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(UserSummary createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreationDateTime() {
        return this.creationDateTime;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Instant getExpirationDateTime() {
        return this.expirationDateTime;
    }

    public void setExpirationDateTime(Instant expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    public Boolean isIsExpired() {
        return this.isExpired;
    }

    public Boolean getIsExpired() {
        return this.isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public Long getSelectedChoice() {
        return this.selectedChoice;
    }

    public void setSelectedChoice(Long selectedChoice) {
        this.selectedChoice = selectedChoice;
    }

    public Long getTotalVotes() {
        return this.totalVotes;
    }

    public void setTotalVotes(Long totalVotes) {
        this.totalVotes = totalVotes;
    }
}
