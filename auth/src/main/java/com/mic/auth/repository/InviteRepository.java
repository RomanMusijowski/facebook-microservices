package com.mic.auth.repository;

import com.mic.auth.domain.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {

    Optional<Invite> findByEventId(Long id);

    List<Invite> findAllByEventId(Long eventId);
}
