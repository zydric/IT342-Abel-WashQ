package edu.cit.abel.washq.dto;

public class AuthResponse {

    private UserDTO user;
    private String accessToken;

    public AuthResponse() {}

    public AuthResponse(UserDTO user, String accessToken) {
        this.user = user;
        this.accessToken = accessToken;
    }

    // --- Getters and Setters ---

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
