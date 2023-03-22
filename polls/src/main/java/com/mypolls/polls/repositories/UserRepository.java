package com.mypolls.polls.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mypolls.polls.models.User;

@Repository
public interface UserRepository extends JpaRepository <User, Long> {        // <model, id>
    Optional <User> findByEmail(String email);
    Optional <User> findByUsername(String username);
    Optional <User> findByUsernameOrEmail(String username, String email);

    List <User> findByIdIn(List <Long> userIds);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
