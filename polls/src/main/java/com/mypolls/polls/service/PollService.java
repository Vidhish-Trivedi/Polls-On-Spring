package com.mypolls.polls.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mypolls.polls.exception.BadRequestException;
import com.mypolls.polls.exception.ResourceNotFoundException;
import com.mypolls.polls.model.Choice;
import com.mypolls.polls.model.ChoiceVoteCount;
import com.mypolls.polls.model.PagedResponse;
import com.mypolls.polls.model.Poll;
import com.mypolls.polls.model.User;
import com.mypolls.polls.model.Vote;
import com.mypolls.polls.model.VoteRequest;
import com.mypolls.polls.payload.PollRequest;
import com.mypolls.polls.payload.PollResponse;
import com.mypolls.polls.repositories.PollRepository;
import com.mypolls.polls.repositories.UserRepository;
import com.mypolls.polls.repositories.VoteRepository;
import com.mypolls.polls.security.UserPrincipal;
import com.mypolls.polls.util.AppConstants;
import com.mypolls.polls.util.ModelMapper;

@Service
public class PollService {
    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(PollService.class);

    private void validatePageNumberAndSize(int page, int size) {
        if(page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }
        if(size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must be under: " + AppConstants.MAX_PAGE_SIZE);
        }
    }

    private Map <Long, Long> getChoiceVoteCountMap(List <Long> pollIds) {
        // Retrieve vote counts of every choice belonging to the given pollIds.
        List <ChoiceVoteCount> votes = voteRepository.countByPollIdInGroupByChoiceId(pollIds);

        Map <Long, Long> choiceVotesMap = votes.stream().collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
        return(choiceVotesMap);
    }

    private Map <Long, Long> getPollUserVoteMap(UserPrincipal currentUser, List <Long> pollIds) {
        // Get votes casted by currently logged-in user to given poll ids.
        Map <Long, Long> pollUserVoteMap = null;

        if(currentUser != null) {
            List <Vote> userVotes = voteRepository.findByUserIdAndPollIdIn(currentUser.getId(), pollIds);

            pollUserVoteMap = userVotes.stream().collect(Collectors.toMap(vote -> vote.getPoll().getId(), vote -> vote.getChoice().getId()));
        }

        return(pollUserVoteMap);
    }

    private Map <Long, User> getPollCreatorMap(List <Poll> polls) {
        // Information of creator of given polls.
        List <Long> creatorIds = polls.stream()
                                    .map(Poll::getCreatedBy)
                                    .distinct()
                                    .collect(Collectors.toList());
        
        List <User> creators = userRepository.findByIdIn(creatorIds);
        
        Map <Long, User> creatorMap = creators.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return(creatorMap);
    }


    public PagedResponse <PollResponse> getAllPolls(UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        // Retrieve polls.
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page <Poll> polls = pollRepository.findAll(pageable);

        if(polls.getNumberOfElements() == 0) {
            return(
                new PagedResponse<>(Collections.emptyList(), polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast())
            );
        }

        // Map polls to poll_responses containing vote counts and poll creator information.
        List <Long> pollIds = polls.map(Poll::getId).getContent();
        Map <Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map <Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
        Map <Long, User> creatorMap = getPollCreatorMap(polls.getContent());

        List <PollResponse> pollResponses = polls.map(poll -> {
            return(
                ModelMapper.mapPollToPollResponse(poll,
                                                choiceVoteCountMap,
                                                creatorMap.get(poll.getCreatedBy()),
                                                pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null))
            );
        }).getContent();

        return(
            new PagedResponse <> (pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast())
        );
    }

    public Poll createPoll(PollRequest pollRequest) {
        Poll poll = new Poll();
        poll.setQuestion(pollRequest.getQuestion());

        pollRequest.getChoices().forEach(choiceRequest -> {
            poll.addChoice(new Choice(choiceRequest.getText()));
        });

        Instant now = Instant.now();
        Instant expirationDateTime = now.plus(Duration.ofDays(pollRequest.getPollLength().getDays())).plus(Duration.ofHours(pollRequest.getPollLength().getHours()));
        poll.setExpirationDateTime(expirationDateTime);

        pollRepository.save(poll);

        return(poll);
    }

    public PollResponse getPollById(Long pollId, UserPrincipal currentUser) {
        Poll poll = pollRepository.findById(pollId).orElseThrow(
            () -> new ResourceNotFoundException("Poll", "id", pollId)
        );

        // Get vote counts of each choice for given poll.
        List <ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

        Map <Long, Long> choiceVotesMap = votes.stream().collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

        // Get information of poll creator.
        User creator = userRepository.findById(poll.getCreatedBy()).orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));

        // Get vote done by logged in user.
        Vote userVote = null;
        if(currentUser != null) {
            userVote = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
        }

        return(
            ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, userVote == null ? null : userVote.getChoice().getId())
        );
    }

    // Method will cast a vote and also get the updated poll after vote has been cast.
    public PollResponse castVote(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser) {
        Poll poll = pollRepository.findById(pollId).orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

        if(poll.getExpirationDateTime().isBefore(Instant.now())) {
            throw new BadRequestException("The requested poll has expired");
        }

        User user = userRepository.getById(currentUser.getId());

        Choice selectedChoice = poll.getChoices().stream()
                                                .filter(choice -> choice.getId().equals(voteRequest.getChoiceId()))
                                                .findFirst()
                                                .orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));

        // Save vote (cast it).
        Vote vote = new Vote();
        vote.setPoll(poll);
        vote.setUser(user);
        vote.setChoice(selectedChoice);

        try {
            vote = voteRepository.save(vote);
        }
        catch (DataIntegrityViolationException ex){
            logger.info("User {} has already voted in Poll {}", currentUser.getId(), pollId);
            throw new BadRequestException("You have already voted in this poll, cannot vote again");
        }

        // Get updated poll.
        List <ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

        Map <Long, Long> choiceVotesMap = votes.stream().collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

        User creator = userRepository.findById(poll.getCreatedBy()).orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));
        
        return(
            ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, vote.getChoice().getId())
        );
    }

    public PagedResponse <PollResponse> getPollsCreatedBy(String username, UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username).orElseThrow(()-> new ResourceNotFoundException("User", "username", username));

        // Get all polls created by user with username.
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page <Poll> polls = pollRepository.findByCreatedBy(user.getId(), pageable);

        if(polls.getNumberOfElements() == 0) {
            return(
                new PagedResponse <> (Collections.emptyList(), polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast())
            );
        }


        List <Long> pollIds = polls.map(Poll::getId).getContent();
        Map <Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map <Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);

        List <PollResponse> pollResponses = polls.map(poll -> {
            return(
                ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, user, pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null))
            );
        }).getContent();

        return(
            new PagedResponse <> (pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast())
        );
    }

    public PagedResponse <PollResponse> getPollsVotedBy(String username, UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username).orElseThrow(()-> new ResourceNotFoundException("User", "username", username));

        // Get all polls created by user with username.
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page <Long> userVotedPollIds = voteRepository.findVotedPollIdsByUserId(user.getId(), pageable);

        if(userVotedPollIds.getNumberOfElements() == 0) {
            return(
                new PagedResponse <> (Collections.emptyList(), userVotedPollIds.getNumber(), userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast())
            );
        }


        List <Long> pollIds = userVotedPollIds.getContent();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List <Poll> polls = pollRepository.findByIdIn(pollIds, sort);

        Map <Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map <Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
        Map <Long, User> creatorMap = getPollCreatorMap(polls);

        List <PollResponse> pollResponses = polls.stream().map(poll -> {
            return(
                ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap, creatorMap.get(poll.getCreatedBy()), pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null))
            );
        }).collect(Collectors.toList());

        return(
            new PagedResponse <> (pollResponses, userVotedPollIds.getNumber(), userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast())
        );
    }
}
