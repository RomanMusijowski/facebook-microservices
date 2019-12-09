package com.mic.eventservice.controller;

import com.mic.eventservice.payload.EventInfo;
import com.mic.eventservice.dto.EventDTO;
import com.mic.eventservice.service.EventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Api(value = "EventController")
@AllArgsConstructor
@RequestMapping("/api/event")
@RestController
public class EventController {

    private final EventService eventService;
    private final ModelMapper modelMapper;

    @GetMapping("/{eventId}")
    public EventInfo getEvent(@PathVariable("eventId") Long eventId){
        return modelMapper.map(eventService.getEventById(eventId), EventInfo.class);
    }


    @ApiOperation(value = "Get all event endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved post.")
    })
    @GetMapping
    public Page<EventDTO> getAllEvents(Pageable pageable) {
        return eventService.getAllEvents(pageable)
                .map(event -> modelMapper.map(event, EventDTO.class));
    }


    @ApiOperation(value = "Get events by date ('yyyy-MM-dd')")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved event.")
    })
    @GetMapping("/date")
    public Page<EventDTO> getAllByDateTimeIsBefore(@RequestParam("range") String range, Pageable pageable) {
        return eventService.getAllByDateTimeIsBefore(pageable, range)
                .map(event -> modelMapper.map(event, EventDTO.class));
    }


    @ApiOperation(value = "Create event endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created event."),
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = {"multipart/form-data"})
    public void createEvent(@RequestPart  String eventDTO,
                            @RequestPart(value = "files") MultipartFile[] files) throws IOException {

        eventService.saveEvent(eventDTO, files);
    }


    @ApiOperation(value = "Join event endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully joined event."),
            @ApiResponse(code = 403, message = "You are not authorized for this action!")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{id}/join")
    public void joinEvent(@PathVariable("id") Long id) {
        eventService.joinEvent(id);
    }


    @ApiOperation(value = "Delete event endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted event."),
            @ApiResponse(code = 403, message = "You are not authorized for this action!")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable("id") Long id) {
        eventService.deleteEvent(id);
    }
}
