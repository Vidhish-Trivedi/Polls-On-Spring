package com.mypolls.polls.controller;

import java.net.URI;
import java.util.Collections;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mypolls.polls.exception.AppException;
import com.mypolls.polls.model.Role;
import com.mypolls.polls.model.RoleName;
import com.mypolls.polls.model.User;
import com.mypolls.polls.payload.ApiResponse;
import com.mypolls.polls.payload.JwtAuthenticationResponse;
import com.mypolls.polls.payload.LoginRequest;
import com.mypolls.polls.payload.SignupRequest;
import com.mypolls.polls.repositories.RoleRepository;
import com.mypolls.polls.repositories.UserRepository;
import com.mypolls.polls.security.JwtTokenProvider;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity <?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = this.authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())
        );
   
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = this.tokenProvider.generateToken(authentication);

        return(ResponseEntity.ok(new JwtAuthenticationResponse(jwt)));
    }

    @PostMapping("/signup")
    public ResponseEntity <?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        // TODO: Check Warnings ?
        if(this.userRepository.existsByUsername(signupRequest.getUsername())) {
            return(
                new ResponseEntity (new ApiResponse(false, "Username is already taken!"),HttpStatus.BAD_REQUEST)
            );
        }

        if(this.userRepository.existsByEmail(signupRequest.getEmail())) {
            return(
                new ResponseEntity (new ApiResponse(false, "Email is already in use!"), HttpStatus.BAD_REQUEST)
            );
        }
    
        // Else, create a new user account.
        User user = new User(signupRequest.getName(), signupRequest.getUsername(), signupRequest.getEmail(), signupRequest.getPassword());

        user.setPassword(this.passwordEncoder.encode(user.getPassword()));

        // System.out.println("---------------" + user.getPassword().length());
        
        Role userRole = this.roleRepository.findByName(RoleName.ROLE_USER)
                                            .orElseThrow(() -> new AppException("User role not set"));

        user.setRoles(Collections.singleton(userRole));

        User result = this.userRepository.save(user);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{username}")
                                                                            .buildAndExpand(result.getUsername())
                                                                            .toUri();
        
        return(ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully!")));
    }
}
