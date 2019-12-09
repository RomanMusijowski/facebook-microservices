package com.mic.eventservice.service;

import com.mic.eventservice.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface EventService {

    Page<Event> getAllEvents(Pageable pageable);

    Event getEventById(Long id);

    void saveEvent(String eventDTO, MultipartFile[] files) throws IOException;

    void joinEvent(Long id);

    void deleteEvent(Long id);

    Page<Event> getAllByDateTimeIsBefore(Pageable pageable, String range);
}