package edu.cit.abel.washq.feature.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import edu.cit.abel.washq.feature.user.User;
import edu.cit.abel.washq.feature.user.UserDTO;
import edu.cit.abel.washq.feature.user.UserRepository;
import edu.cit.abel.washq.shared.exception.DuplicateResourceException;
import edu.cit.abel.washq.shared.exception.InvalidCredentialsException;
import edu.cit.abel.washq.shared.exception.ResourceNotFoundException;
import edu.cit.abel.washq.shared.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${google.client.id:}")
    private String googleClientId;

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

        UserDTO userDTO = mapToDTO(savedUser);

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

        UserDTO userDTO = mapToDTO(user);

        return new AuthResponse(userDTO, token);
    }

    /**
     * Authenticate via Google OAuth.
     * Verifies the Google ID token, creates or retrieves the user, and issues a WashQ JWT.
     */
    public AuthResponse googleLogin(String idTokenString) {
        GoogleIdToken.Payload payload = verifyGoogleToken(idTokenString);
        if (payload == null) {
            throw new InvalidCredentialsException();
        }

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String pictureUrl = (String) payload.get("picture");

        // Find existing user by email or create new one
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Link OAuth if not already linked
            if (user.getOauthProvider() == null) {
                user.setOauthProvider("google");
                user.setOauthId(googleId);
            }
            // Update profile picture if not set
            if (user.getProfilePictureUrl() == null && pictureUrl != null) {
                user.setProfilePictureUrl(pictureUrl);
            }
            user = userRepository.save(user);
        } else {
            user = new User();
            user.setEmail(email);
            user.setFirstName(firstName != null ? firstName : "");
            user.setLastName(lastName != null ? lastName : "");
            user.setOauthProvider("google");
            user.setOauthId(googleId);
            user.setProfilePictureUrl(pictureUrl);
            user.setRole("CUSTOMER");
            // No password for OAuth-only users
            user = userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(mapToDTO(user), token);
    }

    /**
     * Retrieve the currently authenticated user's full profile.
     */
    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToDTO(user);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            }
        } catch (Exception e) {
            System.err.println("Google token verification failed: " + e.getMessage());
        }
        return null;
    }

    private UserDTO mapToDTO(User user) {
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

