package com.mic.auth.controller;

import com.mic.auth.payload.JwtAuthenticationResponse;
import com.mic.auth.payload.LoginRequest;
import com.mic.auth.payload.SignUpRequest;
import com.mic.auth.payload.UserInfo;
import com.mic.auth.service.AuthService;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.validation.Valid;

@Api(value = "AuthController")
@AllArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Login endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully logged in."),
            @ApiResponse(code = 401, message = "Bad credentials.")
    })
    @PostMapping("/signin")
    public JwtAuthenticationResponse authenticateUser(@ApiParam(value = "username or email and password used for authentication.", required = true) @Valid @RequestBody LoginRequest loginRequest) {
        return authService.authenticateUser(loginRequest);
    }

    @ApiOperation(value = "Registration endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully registered user."),
            @ApiResponse(code = 409, message = "Username/Email already in use.")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public void registerUser(@ApiParam(value = "User informations used for register.", required = true) @Valid @RequestBody SignUpRequest signUpRequest) {
        authService.registerUser(signUpRequest);
    }

    @ApiOperation(value = "Current user endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved user."),
            @ApiResponse(code = 403, message = "Forbidden access."),
    })
    @GetMapping("/currentUser")
    public UserInfo getCurrentUserInfo() {
        return modelMapper.map(authService.getCurrentUserInfo(), UserInfo.class);
    }

    @ApiOperation(value = "isAuthenticated endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved user."),
            @ApiResponse(code = 403, message = "Forbidden access."),
    })
    @GetMapping("/isAuthenticated")
    public boolean isAuthenticated(ServletRequest request) {
        return authService.isAuthenticated(request);
    }

    @ApiOperation(value = "User activation endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully activated user.")
    })
    @GetMapping("activation/{userId}")
    public void activateUser(@PathVariable("userId") Long userId,
                             @RequestParam(name = "activationCode") String activationCode) {
        authService.activation(userId, activationCode);
    }
}
