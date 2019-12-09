package com.mic.eventservice.client;

import com.mic.eventservice.payload.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("auth")
public interface AuthClient {

    @GetMapping("api/auth/currentUser")
    UserInfo getCurrentUserInfo();

    @GetMapping("api/user/{userId}")
    UserInfo getUserProfile(@PathVariable("userId") Long userId);

    @PostMapping("api/user/update/{userId}/event/{eventId}")
    void deleteInviteFromUser(@PathVariable("userId")Long userId, @PathVariable("eventId") Long eventId);

    @DeleteMapping("api/user/event/{eventId}")
    void deleteInviteByEventId(@PathVariable("eventId") Long eventId);
}
