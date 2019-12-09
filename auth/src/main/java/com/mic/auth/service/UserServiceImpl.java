package com.mic.auth.service;

import com.mic.auth.client.EventClient;
import com.mic.auth.domain.Invite;
import com.mic.auth.domain.User;
import com.mic.auth.payload.EventInfo;
import com.mic.auth.repository.InviteRepository;
import com.mic.auth.repository.UserRepository;
import com.mic.auth.security.UserPrincipal;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Log4j2
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    public final UserRepository userRepository;
    private final EventClient eventClient;
    private final InviteRepository inviteRepository;

    @Override
    public User getUserProfile(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found!"));
    }

    @Override
    public Page<User> getUserProfiles(Pageable pageable, List<Long> userIds) {
        Page<User> users = userRepository.getAllByIdIn(pageable, userIds);
        users.forEach(user -> user.setFriends(userRepository.getTop10FriendListByUserId(user.getId())));
        return users;
    }

    @Override
    public Page<User> getAllUserFriends(Pageable pageable, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found!");
        }
        return userRepository.getAllByIdIn(pageable, userRepository.getFriendsIdListByUserId(userId));
    }

    @Override
    public void addFriend(Long friendId) {
        User user = getCurrentUser();
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("User you trying to add as friend not found!"));
        if (user.getId().equals(friendId)) {
            throw new DataIntegrityViolationException("You can't be friend with yourself!");
        }
        if (userRepository.existsByIdAndFriendsIdOrIdAndFriendsId(user.getId(), friendId, friendId, user.getId())) {
            throw new DataIntegrityViolationException("You are already friends!");
        }
        user.getFriends().add(friend);
        userRepository.save(user);
    }

    @Override
    public void deleteFriend(Long friendId) {
        User user = getCurrentUser();
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("User you trying to delete from friends not found!"));
        if (!userRepository.existsByIdAndFriendsIdOrIdAndFriendsId(user.getId(), friendId, friendId, user.getId())) {
            throw new EntityNotFoundException("Friend not found!");
        }
        user.getFriends().remove(friend);
        userRepository.save(user);
    }

    @Override
    public void inviteUser( Long eventId, Long userId) {
        if (getCurrentUser().getId().equals(userId)){
            throw new SecurityException("You can't invite yourself.");
        }
        EventInfo event = eventClient.getEvent(eventId);
        User user = getUserProfile(userId);

        user.getInvitedEvents().add(new Invite(null, getCurrentUser().getId(), event.getId(), user));
        userRepository.save(user);
    }

    @Override
    public void deleteInvite(Long userId, Long eventId) {
        User fromDB = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id - " + userId + " not found!"));
        Invite invite = inviteRepository.findByEventId(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Invite with id - " + eventId + " not found!"));

        int toDelete =  fromDB.getInvitedEvents().indexOf(invite);
        fromDB.getInvitedEvents().remove(toDelete);

        userRepository.save(fromDB);
    }

    @Override
    public void deleteInvite(Long eventId){
        List<Invite> invites = inviteRepository.findAllByEventId(eventId);

        invites.stream().forEach(invite -> {
            invite.getInvitedUser().getInvitedEvents().remove(invite);
            userRepository.save(invite.getInvitedUser());
        });
    }

    private User getCurrentUser() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));
    }

    public List<Long> getAllUserFriendsId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found!");
        }
        return userRepository.getFriendsIdListByUserId(userId);
    }
}

