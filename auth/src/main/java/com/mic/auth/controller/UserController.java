package com.mic.auth.controller;

import com.mic.auth.dto.UserDTO;
import com.mic.auth.payload.UserInfo;
import com.mic.auth.util.UserInfoMapper;
import com.mic.auth.service.UserServiceImpl;
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

import java.util.List;

@Api(value = "UserController")
@AllArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserController {

    private final UserServiceImpl userService;
    private final ModelMapper modelMapper;
    private final UserInfoMapper userInfoMapper;

    @ApiOperation(value = "User profile endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully returned user profile."),
            @ApiResponse(code = 400, message = "User not found.")
    })
    @GetMapping("/{userId}")
    public UserInfo getUserProfile(@PathVariable("userId") Long userId) {
        return userInfoMapper.userToUserInfo(userService.getUserProfile(userId));
    }

    @ApiOperation(value = "User profiles endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully returned user profile."),
            @ApiResponse(code = 400, message = "User not found.")
    })

    @GetMapping()
    public List<UserInfo> getUserProfiles(Pageable pageable,
                                          @RequestParam(name = "userIDs") List<Long> userIds) {
        return userService.getUserProfiles(pageable, userIds).map(user -> modelMapper.map(user, UserInfo.class)).getContent();
    }

    @ApiOperation(value = "User friends endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully returned list of user friends id."),
            @ApiResponse(code = 400, message = "User not found.")
    })
    @GetMapping("/{userId}/friendsId")
    public List<Long> getAllUserFriendsId(@PathVariable("userId") Long userId) {
        return userService.getAllUserFriendsId(userId);
    }

    @ApiOperation(value = "User friends endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully returned page of user friends."),
            @ApiResponse(code = 400, message = "User not found.")
    })
    @GetMapping("/{userId}/friend")
    public Page<UserDTO> getAllUserFriends(Pageable pageable, @PathVariable("userId") Long userId) {
        return userService.getAllUserFriends(pageable, userId).map(user -> modelMapper.map(user, UserDTO.class));
    }

    @ApiOperation(value = "Add friend endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully added friend."),
            @ApiResponse(code = 400, message = "User not found."),
            @ApiResponse(code = 409, message = "User can't be friend with yourself or users are already friends")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/friend/{friendId}")
    public void addFriend(@PathVariable("friendId") Long friendId) {
        userService.addFriend(friendId);
    }

    @ApiOperation(value = "Delete friend endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted friend."),
            @ApiResponse(code = 400, message = "User not found.")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/friend/{friendId}")
    public void deleteFriend(@PathVariable("friendId") Long friendId) {
        userService.deleteFriend(friendId);
    }

    @ApiOperation(value = "Invite to a event endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully Invited to a event."),
            @ApiResponse(code = 403, message = "You are not authorized for this action!")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{userId}/invite/{eventId}")
    public void inviteToEvent(@PathVariable("eventId") Long eventId,
                              @PathVariable("userId") Long userId) {
        userService.inviteUser(eventId, userId);
    }

    @ApiOperation(value = "Delete invite from a user endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted invite."),
            @ApiResponse(code = 400, message = "Invite not found."),
            @ApiResponse(code = 400, message = "User not found.")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/update/{userId}/event/{eventId}")
    public void deleteInviteFromUser(@PathVariable("userId")Long userId,@PathVariable("eventId") Long eventId) {

        userService.deleteInvite(userId, eventId);
    }

    @ApiOperation(value = "Delete invite endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted invite."),
            @ApiResponse(code = 400, message = "Invite not found."),
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/event/{eventId}")
    public void deleteInviteByEventId(@PathVariable("eventId") Long eventId) {

        userService.deleteInvite(eventId);
    }
}
