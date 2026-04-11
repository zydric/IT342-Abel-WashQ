package edu.cit.abel.washq.service;

import edu.cit.abel.washq.dto.UserDTO;
import edu.cit.abel.washq.entity.User;
import edu.cit.abel.washq.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    
    private final String UPLOAD_DIR = "uploads/avatars/";

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO updateUserProfile(Long userId, UserDTO updatedFields) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (updatedFields.getFirstName() != null) user.setFirstName(updatedFields.getFirstName());
        if (updatedFields.getLastName() != null) user.setLastName(updatedFields.getLastName());
        if (updatedFields.getAddress() != null) user.setAddress(updatedFields.getAddress());
        if (updatedFields.getContactNumber() != null) user.setContactNumber(updatedFields.getContactNumber());
        
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }
    
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new RuntimeException("FILE-001");
        }
        
        if (file.getSize() > 2 * 1024 * 1024) { // 2MB
            throw new RuntimeException("FILE-001");
        }

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
                    
            String newFilename = "user_" + userId + "_" + UUID.randomUUID().toString() + extension;
            Path path = Paths.get(UPLOAD_DIR + newFilename);
            Files.write(path, file.getBytes());
            
            String fileUrl = "http://localhost:8080/uploads/avatars/" + newFilename;
            
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);
            
            return fileUrl;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
    
    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }
    
    private UserDTO mapToDTO(User user) {
        return new UserDTO(
            user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
            user.getAddress(), user.getContactNumber(), user.getRole(), user.getProfilePictureUrl()
        );
    }
}
