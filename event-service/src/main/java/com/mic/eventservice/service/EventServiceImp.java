package com.mic.eventservice.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mic.eventservice.client.AuthClient;
import com.mic.eventservice.domain.Event;
import com.mic.eventservice.domain.Photo;
import com.mic.eventservice.exceptions.InvalidInputException;
import com.mic.eventservice.repository.EventRepository;
import com.mic.eventservice.repository.PhotoRepository;
import com.mic.eventservice.dto.EventDTO;
import com.mic.eventservice.payload.UserInfo;
import com.mic.s3client.AmazonClient;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@AllArgsConstructor
public class EventServiceImp implements EventService {

    private final AuthClient authClient;
    private final EventRepository eventRepository;
    private final PhotoRepository photoRepository;
    private final AmazonClient amazonClient;

    @Override
    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    @Override
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id - " + id + " not found."));
    }

    @Override
    public void saveEvent(String eventString, MultipartFile[] files) throws IOException {
        UserInfo userInfo = authClient.getCurrentUserInfo();
        EventDTO eventDTO = convertAndValidate(eventString);

        Event event = new Event(null, userInfo.getId(), eventDTO.getName(), eventDTO.getDescription()
                , checkDate(eventDTO.getDateTime()), new HashSet<>(), new ArrayList<>());

        Event savedEvent = eventRepository.save(event);
        addPhoto(files, savedEvent);
        log.info("User " + userInfo.getUsername() + " added a event.");
    }

    private void addPhoto(MultipartFile[] multipartFiles, Event event) {
        List<File> files = Arrays.stream(multipartFiles).map(amazonClient::prepareFiles).collect(Collectors.toList());
        List<String> url = files.stream().map(amazonClient::uploadFile).collect(Collectors.toList());
        url.forEach(s -> photoRepository.save(new Photo(null, event, s)));
    }


    @Override
    public void joinEvent(Long id) {
        UserInfo withInvites = authClient.getUserProfile(authClient.getCurrentUserInfo().getId());

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id -" + id + "doesn't exist"));

        withInvites.getInvitedEvents().forEach(inviteDTO -> {

            if (inviteDTO.getEventId().equals(id)) {
                authClient.deleteInviteFromUser(withInvites.getId(), inviteDTO.getEventId());
            }
        });

        event.getUserIds().add(withInvites.getId());
        eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        UserInfo userInfo = authClient.getCurrentUserInfo();
        Event event = eventRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Event not found!"));

        if (event.getUserId().equals(userInfo.getId())) {
            event.getPhotos().forEach(photo -> {
                amazonClient.deleteFileFromS3Bucket(photo.getUrl());
                photoRepository.delete(photo);
            });
            eventRepository.deleteById(id);
            authClient.deleteInviteByEventId(id);
            log.info("User " + userInfo.getUsername() + " deleted a event.");

        } else {
            throw new SecurityException("You are not authorized for this action!");
        }
    }

    @Override
    public Page<Event> getAllByDateTimeIsBefore(Pageable pageable, String range) {

        LocalDateTime localDate = LocalDateTime.now();
        if (range.equalsIgnoreCase("tomorrow")) {
            localDate.plusDays(1);
        } else if (range.equalsIgnoreCase("week")) {
            localDate.plusDays(7);
        }else {
            throw new InvalidInputException("Invalid input date.");
        }
        return eventRepository.getAllByDateTimeIsBefore(pageable, localDate);
    }

    private LocalDateTime checkDate(String dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime formatDateTime = LocalDateTime.parse(dateTime, formatter);

        if (formatDateTime.isBefore(LocalDateTime.now())){
            throw new InvalidInputException("Date and time must be in the future.");
        }
        return formatDateTime;
    }


    private EventDTO convertAndValidate(String eventString) throws IOException {
        EventDTO eventDTO = new ObjectMapper().readValue(eventString, EventDTO.class);

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.usingContext().getValidator();
        Set<ConstraintViolation<EventDTO>> violations = validator.validate(eventDTO);

        if (!violations.isEmpty()) {
            Set<String> validationMessages = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());

            throw new InvalidInputException(validationMessages.toString());
        }
        return eventDTO;
    }
}
