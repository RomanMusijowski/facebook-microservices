package com.mic.auth.service;

import com.mic.auth.payload.JwtAuthenticationResponse;
import com.mic.auth.payload.LoginRequest;
import com.mic.auth.payload.SignUpRequest;
import com.mic.auth.security.UserPrincipal;

import javax.servlet.ServletRequest;

public interface AuthService {
    JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest);

    void registerUser(SignUpRequest signUpRequest);

    UserPrincipal getCurrentUserInfo();

    boolean isAuthenticated(ServletRequest request);

    void activation(Long userId, String activationCode);
}
