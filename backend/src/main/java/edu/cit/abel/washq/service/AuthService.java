package edu.cit.abel.washq.service;

import edu.cit.abel.washq.dto.*;
import edu.cit.abel.washq.entity.User;
import edu.cit.abel.washq.exception.DuplicateResourceException;
import edu.cit.abel.washq.exception.InvalidCredentialsException;
import edu.cit.abel.washq.exception.ResourceNotFoundException;
import edu.cit.abel.washq.repository.UserRepository;
import edu.cit.abel.washq.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new customer account.
     * Throws DuplicateResourceException if email already exists.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A record with this email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAddress(request.getAddress());
        user.setContactNumber(request.getContactNumber());
        user.setRole("CUSTOMER");

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        UserDTO userDTO = new UserDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getAddress(),
                savedUser.getContactNumber(),
                savedUser.getRole(),
                savedUser.getProfilePictureUrl()
        );

        return new AuthResponse(userDTO, token);
    }

    /**
     * Authenticate with email and password.
     * Throws InvalidCredentialsException if credentials are wrong.
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAddress(),
                user.getContactNumber(),
                user.getRole(),
                user.getProfilePictureUrl()
        );

        return new AuthResponse(userDTO, token);
    }

    /**
     * Retrieve the currently authenticated user's full profile.
     */
    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAddress(),
                user.getContactNumber(),
                user.getRole(),
                user.getProfilePictureUrl()
        );
    }
}
