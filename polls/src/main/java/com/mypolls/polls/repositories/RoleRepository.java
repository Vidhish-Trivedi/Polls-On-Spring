package com.mypolls.polls.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mypolls.polls.model.Role;
import com.mypolls.polls.model.RoleName;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository <Role, Long> {
    Optional <Role> findByName(RoleName roleName);
}
