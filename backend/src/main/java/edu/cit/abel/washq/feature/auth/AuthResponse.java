package edu.cit.abel.washq.feature.auth;

import edu.cit.abel.washq.feature.user.UserDTO;

public class AuthResponse {

    private UserDTO user;
    private String accessToken;

    public AuthResponse() {}

    public AuthResponse(UserDTO user, String accessToken) {
        this.user = user;
        this.accessToken = accessToken;
    }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
