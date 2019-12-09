package com.mic.eventservice.service;


import com.mic.eventservice.domain.Event;
import com.mic.eventservice.exceptions.InvalidInputException;
import com.mic.eventservice.payload.EventInfo;
import com.mic.eventservice.payload.InviteDTO;
import com.mic.eventservice.repository.EventRepository;
import com.mic.eventservice.repository.PhotoRepository;
import com.mic.eventservice.client.AuthClient;
import com.mic.eventservice.dto.EventDTO;
import com.mic.eventservice.payload.UserInfo;
import com.mic.s3client.AmazonClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class EventServiceImpTest {

    @Autowired
    private ModelMapper modelMapper;
    @InjectMocks
    private EventServiceImp eventServiceImp;
    @Mock
    private AuthClient authClient;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private AmazonClient amazonClient;

    private Event event;
    private EventDTO eventDTO;
    private EventInfo eventInfo;
    private UserInfo userInfo;
    private File file;
    private MockMultipartFile mockMultipartFile;
    private MockMultipartFile[] files;
    private Pageable pageable;
    private String eventString;
    private String eventStringErrorDate;
    private String eventStringErrorString;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        modelMapper = new ModelMapper();
        String dateTime = "2025-11-11 11:11";
        LocalDateTime localDateTime = LocalDateTime.now();

        eventInfo = new EventInfo(1L, 2L, "name", "description", localDateTime
                , new ArrayList<>(), "user", localDateTime, localDateTime);

        userInfo = new UserInfo(1L, "username", "email",
                "firstname", "lastname", "phone",
                "gender", Collections.emptyList(), true, new ArrayList<>()
                ,new ArrayList<>(Collections.singleton(new InviteDTO(1L, 2L, 3L))),
                localDateTime, "user", localDateTime);
        event = new Event(1L, 1L, "someName", "description"
                , localDateTime, new HashSet<>(), new ArrayList<>());
        eventString = (" {\n" +
                "\t\"id\":null, \n" +
                "\t\"userId\":null,\n" +
                "\t\"name\":\"someName\",\n" +
                "        \"description\":\"description\",\n" +
                "\t\"dateTime\":\"2025-11-11 11:11\",\n" +
                "\t\"userIds\":null,\n" +
                "\t\"photos\":null\n" +
                "}");
        eventStringErrorDate = (" {\n" +
                "\t\"id\":null, \n" +
                "\t\"userId\":null,\n" +
                "\t\"name\":\"someName\",\n" +
                "        \"description\":\"description\",\n" +
                "\t\"dateTime\":\"2015-11-11 11:11\",\n" +
                "\t\"userIds\":null,\n" +
                "\t\"photos\":null\n" +
                "}");
        eventStringErrorString = (" {\n" +
                "\t\"id\":null, \n" +
                "\t\"userId\":null,\n" +
                "\t\"name\":\"someName\",\n" +
                "        \"description\":\"description\",\n" +
                "\t\"dateTime\":\"2015-11-11 1greghh1\",\n" +
                "\t\"userIds\":null,\n" +
                "\t\"photos\":null\n" +
                "}");
        eventDTO = modelMapper.map(event, EventDTO.class);
        eventDTO.setDateTime(dateTime);
        pageable = PageRequest.of(0, 3);
        String fileName = "test.png";
        file = new File("FileUploadController.targetFolder + fileName");
        mockMultipartFile = new MockMultipartFile("user-file", fileName,
                "photo", "test data".getBytes());
        files = new MockMultipartFile[]{mockMultipartFile};
    }

    @Test
    public void shouldReturnAllEventPage(){
        Mockito.when(eventRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(event)));
        Page<Event> eventPage = eventServiceImp.getAllEvents(pageable);

        assertEquals(1, eventPage.getTotalElements());
        assertEquals( event.getId(), eventPage.getContent().get(0).getId());
        assertEquals( event.getUserId(), eventPage.getContent().get(0).getUserId());
        assertEquals( event.getName(), eventPage.getContent().get(0).getName());
        assertEquals( event.getDateTime(), eventPage.getContent().get(0).getDateTime());
        assertEquals( event.getUserIds(), eventPage.getContent().get(0).getUserIds());
    }

    @Test
    public void shouldReturnEventById(){
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));
        Event returnedEvent = eventServiceImp.getEventById(event.getId());
        assertEquals(event.getId(), returnedEvent.getId());
        assertEquals(event.getUserId(), returnedEvent.getUserId());
        assertEquals(event.getName(), returnedEvent.getName());
        assertEquals(event.getDescription(), returnedEvent.getDescription());
        assertEquals(event.getDateTime(), returnedEvent.getDateTime());
        assertEquals(event.getUserIds(), returnedEvent.getUserIds());
        assertEquals(event.getPhotos(), returnedEvent.getPhotos());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldReturnEntityNotFoundExceptionDuringGetEventById() {
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.empty());
        eventServiceImp.getEventById(event.getId());
    }

    @Test
    public void shouldCreateEvent() throws IOException {
        ArgumentCaptor<Event> argumentCaptor = ArgumentCaptor.forClass(Event.class);

        Mockito.when(authClient.getCurrentUserInfo()).thenReturn(userInfo);
        Mockito.when(amazonClient.prepareFiles(mockMultipartFile)).thenReturn(file);
        Mockito.when(amazonClient.uploadFile(file)).thenReturn("link");
        Mockito.when(eventRepository.save(Mockito.any(Event.class))).thenReturn(event);

        eventServiceImp.saveEvent(eventString, files);

        verify(eventRepository, times(1)).save(argumentCaptor.capture());
        String dateTime = String.valueOf(argumentCaptor.getValue().getDateTime());
        String edited = dateTime.replace("T", " ");

        assertEquals(eventDTO.getDateTime(), edited);
        assertEquals(event.getName(), argumentCaptor.getValue().getName());
        assertEquals(userInfo.getId(), argumentCaptor.getValue().getUserId());
    }

    @Test(expected = InvalidInputException.class)
    public void shouldReturnInvalidInputExceptionAboutDateDuringSaveEvent() throws IOException {
        Mockito.when(authClient.getCurrentUserInfo()).thenReturn(userInfo);
        Mockito.when(eventRepository.save(event)).thenReturn(event);
        eventServiceImp.saveEvent(eventStringErrorDate, files);
    }

    @Test(expected = DateTimeParseException.class)
    public void shouldReturnDateTimeParseExceptionDuringSaveEvent() throws IOException {
        Mockito.when(authClient.getCurrentUserInfo()).thenReturn(userInfo);
        Mockito.when(eventRepository.save(event)).thenReturn(event);
        eventServiceImp.saveEvent(eventStringErrorString, files);
    }

    @Test
    public void shouldJoinToEvent(){
        Mockito.when(authClient.getUserProfile(userInfo.getId())).thenReturn(userInfo);
        Mockito.when(authClient.getCurrentUserInfo()).thenReturn(userInfo);
        Mockito.when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        eventServiceImp.joinEvent(event.getId());
        verify(eventRepository, times(1)).findById(event.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldReturnEntityNotFoundExceptionJoinToEvent(){
        Mockito.when(authClient.getCurrentUserInfo()).thenReturn(userInfo);
        Mockito.when(eventRepository.findById(event.getId())).thenReturn(Optional.empty());
        eventServiceImp.joinEvent(event.getId());
    }

    @Test
    public void shouldDeleteEvent() {
        Mockito.when(authClient.getCurrentUserInfo()).thenReturn(userInfo);
        Mockito.when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        eventServiceImp.deleteEvent(event.getId());
        verify(eventRepository, times(1)).deleteById(event.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldReturnEntityNotFoundExceptionDuringDeleting() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);

        eventServiceImp.deleteEvent(1L);
    }

    @Test(expected = SecurityException.class)
    public void shouldReturnSecurityExceptionDuringDeleting() {
        userInfo.setId(2L);
        Mockito.when(authClient.getCurrentUserInfo()).thenReturn(userInfo);
        Mockito.when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        eventServiceImp.deleteEvent(event.getId());
    }

    @Test
    public void shouldReturnPageOfEvent(){

        Mockito.when(eventRepository.getAllByDateTimeIsBefore(any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(event)));

        Page<Event> eventPage = eventServiceImp.getAllByDateTimeIsBefore(pageable, "week");

        assertEquals(1, eventPage.getTotalElements());
        assertEquals( event.getId(), eventPage.getContent().get(0).getId());
        assertEquals( event.getUserId(), eventPage.getContent().get(0).getUserId());
        assertEquals( event.getName(), eventPage.getContent().get(0).getName());
        assertEquals( event.getDateTime(), eventPage.getContent().get(0).getDateTime());
        assertEquals( event.getUserIds(), eventPage.getContent().get(0).getUserIds());
    }
}