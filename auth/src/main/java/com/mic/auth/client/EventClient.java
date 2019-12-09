package com.mic.auth.client;

import com.mic.auth.payload.EventInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient("event")
public interface EventClient {

    @GetMapping("api/event/{eventId}")
    EventInfo getEvent(@PathVariable("eventId") Long eventId);
}
