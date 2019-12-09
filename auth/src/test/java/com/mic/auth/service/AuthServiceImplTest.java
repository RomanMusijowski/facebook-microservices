package com.mic.auth.service;

import com.mic.auth.domain.Role;
import com.mic.auth.domain.RoleName;
import com.mic.auth.domain.User;
import com.mic.auth.exception.ActivationException;
import com.mic.auth.exception.UserRegistrationException;
import com.mic.auth.payload.JwtAuthenticationResponse;
import com.mic.auth.payload.LoginRequest;
import com.mic.auth.payload.SignUpRequest;
import com.mic.auth.repository.RoleRepository;
import com.mic.auth.repository.UserRepository;
import com.mic.auth.security.JwtTokenProvider;
import com.mic.auth.security.UserPrincipal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private Authentication authentication;
    @SuppressWarnings("unused")
    @Mock
    private MailService mailService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Random rand;

    private LoginRequest loginRequest;
    private UserPrincipal userPrincipal;
    private UsernamePasswordAuthenticationToken upat;
    private SignUpRequest signUpRequest;
    private Role role;
    private Set<Role> roles;
    private User user;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        String username = "username";
        String password = "password";
        userPrincipal = new UserPrincipal(1L, "username", "password",
                "email", new ArrayList<>(), "firstname", "lastname",
                "phone", "gender", Collections.emptyList(), true, "MURGT");
        loginRequest = new LoginRequest(username, password);
        upat = new UsernamePasswordAuthenticationToken(username, password);
        signUpRequest = new SignUpRequest("username",
                "password", "email", "firstname",
                "lastname", "598665214", "male");
        role = new Role(2L, RoleName.ROLE_USER);
        roles = new HashSet<>();
        roles.add(role);
        user = new User(null, "username", "encodedPassword",
                "email", roles, "firstname", "lastname",
                "598665214", "male", Collections.emptyList(), false, "YRJGY", new ArrayList<>());

    }

    @Test
    public void shouldAuthenticateUser() {
        Mockito.when(authenticationManager.authenticate(upat))
                .thenReturn(authentication);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        Mockito.when(tokenProvider.generateToken(authentication))
                .thenReturn("asdasdasdas");
        JwtAuthenticationResponse out = authService.authenticateUser(loginRequest);
        Assert.assertEquals("Bearer", out.getTokenType());
        Assert.assertEquals("asdasdasdas", out.getAccessToken());
    }

    @Test
    public void shouldRegisterUser() {
        Mockito.when(userRepository.existsByUsername(signUpRequest.getUsername()))
                .thenReturn(false);
        Mockito.when(userRepository.existsByEmail(signUpRequest.getEmail()))
                .thenReturn(false);
        Mockito.when(passwordEncoder.encode(signUpRequest.getPassword()))
                .thenReturn("encodedPassword");
        Mockito.when(rand.nextInt(26))
                .thenReturn(35);
        Mockito.when(roleRepository.findByName(RoleName.ROLE_USER))
                .thenReturn(Optional.of(role));
        Mockito.when(userRepository.getByUsername("name"))
                .thenReturn(user);
        authService.registerUser(signUpRequest);
        verify(userRepository, times(1)).save(Mockito.any(User.class));
    }

    @Test(expected = UserRegistrationException.class)
    public void shouldThrowUserRegistrationExceptionWhenUsernameIsTaken() {
        Mockito.when(userRepository.existsByUsername(signUpRequest.getUsername()))
                .thenReturn(true);
        Mockito.when(userRepository.existsByEmail(signUpRequest.getEmail()))
                .thenReturn(false);
        authService.registerUser(signUpRequest);
    }

    @Test(expected = UserRegistrationException.class)
    public void shouldThrowUserRegistrationExceptionWhenEmailIsTaken() {
        Mockito.when(userRepository.existsByUsername(signUpRequest.getUsername()))
                .thenReturn(false);
        Mockito.when(userRepository.existsByEmail(signUpRequest.getEmail()))
                .thenReturn(true);

        authService.registerUser(signUpRequest);
    }

    @Test
    public void shouldReturnCurrentUserInfo() {
        Mockito.when(securityContext.getAuthentication())
                .thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        UserPrincipal out = authService.getCurrentUserInfo();
        Assert.assertEquals(userPrincipal.getId(), out.getId());
        Assert.assertEquals(userPrincipal.getUsername(), out.getUsername());
        Assert.assertEquals(userPrincipal.getEmail(), out.getEmail());
    }

    @Test
    public void shouldActivateAccount() {
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        authService.activation(user.getId(), user.getActivationCode());
        verify(userRepository, times(1)).save(user);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringActivateAccountWhenUserIsNotFound() {
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        authService.activation(2L, user.getActivationCode());
    }

    @Test(expected = ActivationException.class)
    public void shouldThrowActivationExceptionDuringActivateAccountWhenActivationCodeIsNotValid() {
        user.setId(1l);
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        authService.activation(1L, "");
    }
}