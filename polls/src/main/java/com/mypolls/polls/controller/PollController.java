package com.mypolls.polls.controller;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mypolls.polls.model.PagedResponse;
import com.mypolls.polls.model.Poll;
import com.mypolls.polls.model.VoteRequest;
import com.mypolls.polls.payload.ApiResponse;
import com.mypolls.polls.payload.PollRequest;
import com.mypolls.polls.payload.PollResponse;
import com.mypolls.polls.security.CurrentUser;
import com.mypolls.polls.security.UserPrincipal;
import com.mypolls.polls.service.PollService;
import com.mypolls.polls.util.AppConstants;

@RestController
@RequestMapping("api/polls")
public class PollController {

    @Autowired
    private PollService pollService;

    @GetMapping
    public PagedResponse <PollResponse> getPolls(@CurrentUser UserPrincipal currentUser,
                                                @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
                                                    return(pollService.getAllPolls(currentUser, page, size));
                                                }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity <?> createPoll(@Valid @RequestBody PollRequest pollRequest) {
        Poll poll = pollService.createPoll(pollRequest);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{pollId}").buildAndExpand(poll.getId()).toUri();

        return(
            ResponseEntity.created(location).body(new ApiResponse(true, "Poll created successfully"))
        );
    }

    @GetMapping("/{pollId}")
    public PollResponse getPollById(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId) {
        return(
            pollService.getPollById(pollId, currentUser)
        );
    }

    @PostMapping("/{pollId}/votes")
    @PreAuthorize("hasRole('USER')")
    public PollResponse castVote(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId, @Valid @RequestBody VoteRequest voteRequest) {
        return(
            pollService.castVote(pollId, voteRequest, currentUser)              // Will cast vote and also return updated poll information.
        );
    }

}
