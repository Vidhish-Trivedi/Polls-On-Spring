package com.mypolls.polls.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mypolls.polls.model.User;
import com.mypolls.polls.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Login with either username or email, used by spring security.
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElseThrow(
            () -> new UsernameNotFoundException("User with given username/email not found: " + usernameOrEmail)
        );
    
        return(UserPrincipal.create(user));
    }

    // To be used by JWTAuthenticationFilter.
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
            () -> new UsernameNotFoundException("User with given id not found: " + id)
        );

        return(UserPrincipal.create(user));
    }
}
