package com.mypolls.polls.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mypolls.polls.model.Poll;

@Repository
public interface PollRepository extends JpaRepository <Poll, Long> {
    Optional <Poll> findById(Long pollId);

    Page <Poll> findByCreatedBy(Long userId, Pageable pageable);

    Long countByCreatedBy(Long userId);

    List <Poll> findByIdIn(List <Long> pollIds);
    List <Poll> findByIdIn(List <Long> pollIds, Sort sort);
}
