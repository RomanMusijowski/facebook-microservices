package com.mic.post.client;


import com.mic.post.payload.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("auth")
public interface AuthClient {
    @GetMapping("api/auth/currentUser")
    UserInfo getCurrentUserInfo();

    @GetMapping("api/user")
    List<UserInfo> getUserProfiles(Pageable pageable,
                                   @RequestParam(name = "userIDs") List<Long> userIds);

    @GetMapping("api/user/{userId}/friendsId")
    List<Long> getAllUserFriendsId(@PathVariable("userId") Long userId);
}
