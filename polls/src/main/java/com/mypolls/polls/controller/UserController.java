package com.mypolls.polls.controller;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mypolls.polls.exception.ResourceNotFoundException;
import com.mypolls.polls.model.PagedResponse;
import com.mypolls.polls.model.User;
import com.mypolls.polls.payload.PollResponse;
import com.mypolls.polls.payload.UserIdentityAvailability;
import com.mypolls.polls.payload.UserProfile;
import com.mypolls.polls.payload.UserSummary;
import com.mypolls.polls.repositories.PollRepository;
import com.mypolls.polls.repositories.UserRepository;
import com.mypolls.polls.repositories.VoteRepository;
import com.mypolls.polls.security.CurrentUser;
import com.mypolls.polls.security.UserPrincipal;
import com.mypolls.polls.service.PollService;
import com.mypolls.polls.util.AppConstants;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PollService pollService;

    // private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        UserSummary userSummary = new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName());
        return(userSummary);        // Profile of currently logged-in user.
    }

    @GetMapping("/user/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        Boolean isAvailable = ! (userRepository.existsByUsername(username));
        return(
            new UserIdentityAvailability(isAvailable)
        );
    }

    @GetMapping("/users/{username}")
    public UserProfile getUserProfile(@PathVariable(value = "username") String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Long pollCount = pollRepository.countByCreatedBy(user.getId());
        Long voteCount = voteRepository.countByUserId(user.getId());

        UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt(), pollCount, voteCount);

        return(userProfile);
    }

    // Get all polls created by logged-in user.
    @GetMapping("/users/{username}/polls")
    public PagedResponse <PollResponse> getPollsByCreatedBy(@PathVariable(value = "username") String username,
                                                            @CurrentUser UserPrincipal currentUser,
                                                            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
                                                        ) {
                                                            return(
                                                                pollService.getPollsCreatedBy(username, currentUser, page, size)
                                                            );
                                                        }
                                    
    // Get all polls in which the logged-in user has voted.
    @GetMapping("/users/{username}/votes")
    public PagedResponse <PollResponse> getPollsVotedBy(@PathVariable(value = "username") String username,
                                                        @CurrentUser UserPrincipal currentUser,
                                                        @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                        @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
                                                    ) {
                                                        return(
                                                            pollService.getPollsVotedBy(username, currentUser, page, size)
                                                        );
                                                    }
}
