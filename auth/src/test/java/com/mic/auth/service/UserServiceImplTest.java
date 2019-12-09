package com.mic.auth.service;

import com.mic.auth.client.EventClient;
import com.mic.auth.domain.Invite;
import com.mic.auth.domain.User;
import com.mic.auth.payload.EventInfo;
import com.mic.auth.repository.InviteRepository;
import com.mic.auth.repository.UserRepository;
import com.mic.auth.security.UserPrincipal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InviteRepository inviteRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private EventClient eventClient;

    private User user;
    private User user2;
    private User friend;
    private List<Long> friendIdList;
    private UserPrincipal userPrincipal;
    private Pageable pageable;
    private EventInfo event;
    Invite invite;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        String dateTime = "2025-11-11 11:11";
        LocalDateTime localDateTime = LocalDateTime.now();
        event = new EventInfo(1L, 1L, "someName", "description"
                , localDateTime, new ArrayList<>(),localDateTime, "user", localDateTime);
        invite = new Invite(1L,1L ,event.getId(), null);
        user = new User(1L, "username", "password",
                "email", new HashSet<>(), "firstname", "lastname",
                "phone", "gender", new ArrayList<>(), true, "MURGT", new ArrayList<>());
        user2 = new User(2L, "username", "password",
                "email", new HashSet<>(), "firstname", "lastname",
                "phone", "gender", new ArrayList<>(), true, "MURGT"
                , new ArrayList<>(Collections.singleton(invite)));
        friend = new User(2L, "username2", "password",
                "email2", new HashSet<>(), "firstname2", "lastname2",
                "phone2", "gender2", Collections.emptyList(), true, "MUSGT", new ArrayList<>());
        userPrincipal = new UserPrincipal(1L, "username", "password",
                "email", new ArrayList<>(), "firstname", "lastname",
                "phone", "gender", Collections.emptyList(), true, "MURGT");
        friendIdList = new ArrayList<>();
        friendIdList.add(friend.getId());
        pageable = PageRequest.of(0, 3);
    }

    @Test
    public void shouldReturnUserProfile() {
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        User out = userService.getUserProfile(user.getId());
        Assert.assertEquals(User.class, out.getClass());
        Assert.assertEquals(user.getId(), out.getId());
        Assert.assertEquals(user.getUsername(), out.getUsername());
        Assert.assertEquals(user.getEmail(), out.getEmail());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringReturningUserProfileWhenUserDoNotExist() {
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.empty());
        userService.getUserProfile(user.getId());
    }

    @Test
    public void shouldReturnPageOfUserFriends() {
        user.setFriends(Collections.singletonList(friend));
        Mockito.when(userRepository.existsById(user.getId()))
                .thenReturn(true);
        Mockito.when(userRepository.getFriendsIdListByUserId(user.getId()))
                .thenReturn(friendIdList);
        Mockito.when(userRepository.getAllByIdIn(pageable, friendIdList))
                .thenReturn(new PageImpl<>(user.getFriends()));
        Page<User> out = userService.getAllUserFriends(pageable, user.getId());
        Assert.assertEquals(friend.getId(), out.getContent().get(0).getId());
        Assert.assertEquals(friend.getUsername(), out.getContent().get(0).getUsername());
        Assert.assertEquals(friend.getEmail(), out.getContent().get(0).getEmail());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringGettingPageOfUserFriendsWhenUserDoNotExist() {
        Mockito.when(userRepository.existsById(user.getId()))
                .thenReturn(false);
        userService.getAllUserFriends(pageable, user.getId());
    }

    @Test
    public void shouldAddFriend() {
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(friend.getId()))
                .thenReturn(Optional.of(friend));
        Mockito.when(userRepository.existsByIdAndFriendsIdOrIdAndFriendsId(user.getId(), friend.getId(), friend.getId(), user.getId()))
                .thenReturn(false);
        userService.addFriend(friend.getId());
        Mockito.verify(userRepository, times(1)).save(user);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringAddingFriendWhenUserDoNotExist() {
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        Mockito.when(userRepository.findById(userPrincipal.getId()))
                .thenReturn(Optional.empty());
        userService.addFriend(userPrincipal.getId());
        verify(userService, times(1)).addFriend(userPrincipal.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringAddingFriendWhenFriendDoNotExistInDB() {
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(friend.getId()))
                .thenReturn(Optional.empty());
        userService.addFriend(friend.getId());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void shouldThrowDataIntegrityViolationExceptionDuringAddingFriendWhenUserIsTryingToAddYourselfAsFriend() {
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        userService.addFriend(user.getId());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void shouldThrowDataIntegrityViolationExceptionDuringAddingFriendWhenUsersAreAlredyFriend() {
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(friend.getId()))
                .thenReturn(Optional.of(friend));
        Mockito.when(userRepository.existsByIdAndFriendsIdOrIdAndFriendsId(user.getId(), friend.getId(), friend.getId(), user.getId()))
                .thenReturn(true);
        userService.addFriend(friend.getId());
    }

    @Test
    public void shouldDeleteFriend() {
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(friend.getId()))
                .thenReturn(Optional.of(friend));
        Mockito.when(userRepository.existsByIdAndFriendsIdOrIdAndFriendsId(user.getId(), friend.getId(), friend.getId(), user.getId()))
                .thenReturn(true);
        userService.deleteFriend(friend.getId());
        verify(userRepository, times(1)).save(user);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringDeleteFriendWhenUserDoNotExist() {
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        Mockito.when(userRepository.findById(userPrincipal.getId()))
                .thenReturn(Optional.empty());
        userService.deleteFriend(2L);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringDeleteFriendWhenFriendDoNotExistInDB() {
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.empty());
        userService.deleteFriend(2L);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringDeleteFriendWhenUsersAreNotFriends() {
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(friend.getId()))
                .thenReturn(Optional.of(friend));
        Mockito.when(userRepository.existsByIdAndFriendsIdOrIdAndFriendsId(user.getId(), friend.getId(), friend.getId(), user.getId()))
                .thenReturn(false);
        userService.deleteFriend(friend.getId());
    }

    @Test
    public void shouldReturnPageOfUserProfiles() {
        Mockito.when(userRepository.getAllByIdIn(pageable, friendIdList))
                .thenReturn(new PageImpl<>(Collections.singletonList(friend)));
        Page<User> out = userService.getUserProfiles(pageable, Collections.singletonList(friend.getId()));
        Assert.assertEquals(friend.getId(), out.getContent().get(0).getId());
        Assert.assertEquals(friend.getUsername(), out.getContent().get(0).getUsername());
        Assert.assertEquals(friend.getEmail(), out.getContent().get(0).getEmail());
    }

    @Test
    public void shouldInviteUserToEvent() {
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal()).thenReturn(userPrincipal);
        Mockito.when(userRepository.findById(userPrincipal.getId())).thenReturn(Optional.ofNullable(user));
        Mockito.when(eventClient.getEvent(event.getId())).thenReturn(event);
        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.ofNullable(user2));

        userService.inviteUser( event.getId() ,user2.getId());
        verify(userRepository, times(1)).save(argumentCaptor.capture());
        assertEquals(user2.getId(), argumentCaptor.getValue().getId());
        assertEquals(user2.getUsername(), argumentCaptor.getValue().getUsername());
        assertEquals(user2.getEmail(), argumentCaptor.getValue().getEmail());
        assertEquals(user2.getActivationCode(), argumentCaptor.getValue().getActivationCode());
        assertEquals(user2.getFriends(), argumentCaptor.getValue().getFriends());
        assertEquals(user2.getInvitedEvents(), argumentCaptor.getValue().getInvitedEvents());
        assertEquals(user2.getFirstName(), argumentCaptor.getValue().getFirstName());
        assertEquals(user2.getLastName(), argumentCaptor.getValue().getLastName());
        assertEquals(user2.getGender(), argumentCaptor.getValue().getGender());
        assertEquals(user2.getPhoneNumber(), argumentCaptor.getValue().getPhoneNumber());
        assertEquals(user2.getPassword(), argumentCaptor.getValue().getPassword());

        verify(userRepository, times(1)).save(user2);
    }

    @Test
    public void shouldDeleteInviteFromUser(){
        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.ofNullable(user2));
        Mockito.when(inviteRepository.findByEventId(event.getId())).thenReturn(Optional.ofNullable(invite));

        userService.deleteInvite(user2.getId(), event.getId());

        verify(userRepository,times(1)).save(user2);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDeleteInviteFromUser(){
        Mockito.when(inviteRepository.findByEventId(event.getId())).thenReturn(Optional.ofNullable(invite));
        userService.deleteInvite(user2.getId(), event.getId());
    }

    @Test
    public void shouldDeleteExistingInvites(){
        invite.setInvitedUser(user2);
        Mockito.when(inviteRepository.findAllByEventId(event.getId()))
                .thenReturn(Collections.singletonList(invite));

        userService.deleteInvite(event.getId());
        verify(userRepository, times(1)).save(user2);
    }
}


