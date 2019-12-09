package com.mic.eventservice.repository;

import com.mic.eventservice.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> getAllByDateTimeIsBefore(Pageable pageable, LocalDateTime meetingTime);

    Page<Event> findAll(Pageable pageable);
}
