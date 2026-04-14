package ru.mws.wiki.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mws.wiki.dto.AuthResponse;
import ru.mws.wiki.dto.LoginRequest;
import ru.mws.wiki.dto.RegisterRequest;
import ru.mws.wiki.entity.Role;
import ru.mws.wiki.entity.User;
import ru.mws.wiki.exception.ConflictException;
import ru.mws.wiki.repository.UserRepository;
import ru.mws.wiki.security.JwtService;

/**
 * Service handling user registration and authentication.
 *
 * <p>Registration creates a new {@link User} with an encoded password.
 * Login authenticates via Spring Security and returns a JWT token.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user account.
     *
     * @param request registration data
     * @return JWT token and user info
     * @throws ConflictException if username or email is already taken
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName() != null ? request.displayName() : request.username())
                .role(Role.USER)
                .build();

        userRepository.save(user);
        log.info("Registered new user: {}", user.getUsername());

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole(), user.getDisplayName());
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * @param request login credentials
     * @return JWT token and user info
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow();

        log.info("User logged in: {}", user.getUsername());
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole(), user.getDisplayName());
    }
}
