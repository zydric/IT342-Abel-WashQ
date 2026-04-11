package edu.cit.abel.washq.controller;

import edu.cit.abel.washq.dto.ApiResponse;
import edu.cit.abel.washq.dto.UserDTO;
import edu.cit.abel.washq.entity.User;
import edu.cit.abel.washq.repository.UserRepository;
import edu.cit.abel.washq.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }
    
    private void verifyOwnership(Long id, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Unauthorized"));
                
        // Allow if user is ADMIN or the target user id matches their own
        if (!user.getId().equals(id) && !user.getRole().equals("ADMIN")) {
            throw new RuntimeException("AUTH-003: Cannot update another user's profile");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getMe(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Unauthorized"));
            UserDTO profile = userService.getUserProfile(user.getId());
            return ResponseEntity.ok(ApiResponse.success(profile));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("UNAUTHORIZED", "Failed to get user profile", null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @PathVariable Long id, 
            @RequestBody UserDTO updatedFields,
            Authentication authentication) {
        try {
            verifyOwnership(id, authentication);
            UserDTO updatedUser = userService.updateUserProfile(id, updatedFields);
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (RuntimeException e) {
            String code = e.getMessage().startsWith("AUTH-003") ? "AUTH-003" : "USER_UPDATE_ERROR";
            int status = e.getMessage().startsWith("AUTH-003") ? 403 : 400;
            return ResponseEntity.status(status).body(ApiResponse.error(code, e.getMessage(), null));
        }
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @PathVariable Long id, 
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            verifyOwnership(id, authentication);
            String url = userService.uploadAvatar(id, file);
            
            Map<String, String> responseData = new HashMap<>();
            responseData.put("profilePictureUrl", url);
            
            return ResponseEntity.ok(ApiResponse.success(responseData));
        } catch (RuntimeException e) {
            String code = e.getMessage().startsWith("FILE-001") ? "FILE-001" : "UPLOAD_ERROR";
            int status = e.getMessage().startsWith("FILE-001") ? 400 : 500;
            return ResponseEntity.status(status).body(ApiResponse.error(code, e.getMessage(), null));
        }
    }
}
