package com.mypolls.polls.util;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mypolls.polls.model.Poll;
import com.mypolls.polls.model.User;
import com.mypolls.polls.payload.ChoiceResponse;
import com.mypolls.polls.payload.PollResponse;
import com.mypolls.polls.payload.UserSummary;

public class ModelMapper {
    public static PollResponse mapPollToPollResponse(Poll poll, Map <Long, Long> choiceVotesMap, User creator, Long userVoteId) {
        PollResponse pollResponse = new PollResponse();
        pollResponse.setId(poll.getId());
        pollResponse.setQuestion(poll.getQuestion());
        pollResponse.setCreationDateTime(poll.getCreatedAt());
        pollResponse.setExpirationDateTime(poll.getExpirationDateTime());
        Instant now = Instant.now();
        pollResponse.setIsExpired(poll.getExpirationDateTime().isBefore(now));

        List <ChoiceResponse> choiceResponses = poll.getChoices().stream().map(choice -> {
            ChoiceResponse choiceResponse = new ChoiceResponse();
            choiceResponse.setId(choice.getId());
            choiceResponse.setText(choice.getText());

            if(choiceVotesMap.containsKey(choice.getId())) {
                choiceResponse.setVoteCount(choiceVotesMap.get(choice.getId()));
            }
            else {
                choiceResponse.setVoteCount(Long.parseLong("0"));
            }

            return(choiceResponse);
        }).collect(Collectors.toList());

        pollResponse.setChoices(choiceResponses);
        UserSummary creatorSummary = new UserSummary(creator.getId(), creator.getUsername(), creator.getName());
        pollResponse.setCreatedBy(creatorSummary);

        if(userVoteId != null) {
            pollResponse.setSelectedChoice(userVoteId);
        }

        Long totalVotes = pollResponse.getChoices().stream().mapToLong(ChoiceResponse::getVoteCount).sum();
        pollResponse.setTotalVotes(totalVotes);

        return(pollResponse);
    }    
}
