package edu.cit.abel.washq.controller;

import edu.cit.abel.washq.dto.*;
import edu.cit.abel.washq.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /auth/register
     * Register a new customer account.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    /**
     * POST /auth/login
     * Authenticate with email and password.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * GET /auth/me
     * Retrieve the current authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserDTO data = authService.getCurrentUser(email);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
