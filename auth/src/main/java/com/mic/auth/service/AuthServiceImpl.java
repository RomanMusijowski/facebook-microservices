package com.mic.auth.service;

import com.mic.auth.domain.Role;
import com.mic.auth.domain.RoleName;
import com.mic.auth.domain.User;
import com.mic.auth.exception.ActivationException;
import com.mic.auth.exception.AppException;
import com.mic.auth.exception.UserRegistrationException;
import com.mic.auth.payload.JwtAuthenticationResponse;
import com.mic.auth.payload.LoginRequest;
import com.mic.auth.payload.SignUpRequest;
import com.mic.auth.repository.RoleRepository;
import com.mic.auth.repository.UserRepository;
import com.mic.auth.security.JwtTokenProvider;
import com.mic.auth.security.UserPrincipal;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

@Service
@AllArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final MailService mailService;
    private final Random rand = new Random();

    @Override
    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        log.info("User " + loginRequest.getUsernameOrEmail() + " has logged in.");
        return new JwtAuthenticationResponse(jwt);
    }

    @Override
    public void registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new UserRegistrationException("Username already in use!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new UserRegistrationException("Email Address already in use!");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));

        User user = new User(null, signUpRequest.getUsername(), signUpRequest.getPassword(),
                signUpRequest.getEmail(), new HashSet<>(Collections.singletonList(userRole)), signUpRequest.getFirstName(), signUpRequest.getLastName(),
                signUpRequest.getPhoneNumber(), signUpRequest.getGender(), new ArrayList<>(), false, getRandomActivationCode(), new ArrayList<>());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        mailService.sendActivationMail(userRepository.getByUsername(user.getUsername()));
        log.info("User " + user.getUsername() + " has been created.");
    }

    @Override
    public UserPrincipal getCurrentUserInfo() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public boolean isAuthenticated(ServletRequest request) {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String jwt = httpRequest.getHeader("authorization");
            Long userId = null;
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                userId = tokenProvider.getUserIdFromJWT(jwt);
            } else {
                throw new AccessDeniedException("Access denied!");
            }
            UserPrincipal userPrincipal = getCurrentUserInfo();
            return userPrincipal.getId().equals(userId);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    @Override
    public void activation(Long userId, String activationCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));
        if (user.getActivationCode().equals(activationCode)) {
            user.setActive(true);
            user.setMailActivatedDate(LocalDateTime.now());
            userRepository.save(user);
            log.info("User " + user.getUsername() + " has activated the account.");
        } else {
            throw new ActivationException("Activation code is not valid!");
        }
    }

    private String getRandomActivationCode() {
        StringBuilder activationCode = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            activationCode.append((char) (rand.nextInt(26) + 65));
        }
        return activationCode.toString();
    }

}
