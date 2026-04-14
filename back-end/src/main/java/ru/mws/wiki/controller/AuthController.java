package ru.mws.wiki.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.mws.wiki.dto.AuthResponse;
import ru.mws.wiki.dto.LoginRequest;
import ru.mws.wiki.dto.RegisterRequest;
import ru.mws.wiki.service.AuthService;

/**
 * REST controller for user authentication (register and login).
 *
 * <p>All endpoints are public — no JWT required.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account.
     *
     * @param request registration data (validated)
     * @return JWT token and user info
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for username: {}", request.username());
        return authService.register(request);
    }

    /**
     * Authenticates an existing user.
     *
     * @param request login credentials (validated)
     * @return JWT token and user info
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.username());
        return authService.login(request);
    }
}
