package com.mic.auth.service;

import com.mic.auth.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    User getUserProfile(Long userId);

    Page<User> getUserProfiles(Pageable pageable, List<Long> userIds);

    Page<User> getAllUserFriends(Pageable pageable, Long userId);

    void addFriend(Long friendId);

    void deleteFriend(Long friendId);

    void inviteUser(Long userId, Long eventId);

    void deleteInvite(Long userId, Long eventId);

    void deleteInvite(Long eventId);
}
