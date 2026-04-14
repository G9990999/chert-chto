package ru.mws.wiki;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.mws.wiki.dto.AuthResponse;
import ru.mws.wiki.dto.LoginRequest;
import ru.mws.wiki.dto.RegisterRequest;
import ru.mws.wiki.entity.Role;
import ru.mws.wiki.entity.User;
import ru.mws.wiki.exception.ConflictException;
import ru.mws.wiki.repository.UserRepository;
import ru.mws.wiki.security.JwtService;
import ru.mws.wiki.service.AuthService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 *
 * <p>Uses Mockito to isolate the service from the database and JWT layer.</p>
 */
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest("alice", "alice@example.com", "password123", "Alice");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        User saved = User.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .email("alice@example.com")
                .passwordHash("hashed")
                .displayName("Alice")
                .role(Role.USER)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertEquals("alice", response.username());
        assertEquals("jwt-token", response.token());
        assertEquals(Role.USER, response.role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateUsername_throwsConflict() {
        RegisterRequest req = new RegisterRequest("alice", "alice@example.com", "password123", null);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        RegisterRequest req = new RegisterRequest("alice", "alice@example.com", "password123", null);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest("alice", "password123");
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .role(Role.USER)
                .displayName("Alice")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertEquals("alice", response.username());
        assertEquals("jwt-token", response.token());
    }
}
