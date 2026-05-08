package edu.cit.abel.washq.feature.auth;

import edu.cit.abel.washq.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthControllerTest — Vertical Slice: feature/auth
 * Tests all authentication endpoints using MockMvc + H2 in-memory database.
 * Extends BaseIntegrationTest to ensure H2 overrides Supabase PostgreSQL settings.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // -----------------------------------------------------------------------
    // TC-AUTH-001: Register new user with valid data → expects 201, returns JWT
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-AUTH-001: Register with valid data returns 201 and JWT")
    void TC_AUTH_001_register_withValidData_returns201() throws Exception {
        String body = """
            {"email":"tc001@washq.com","password":"pass1234",
             "firstName":"John","lastName":"Doe",
             "address":"Cebu City","contactNumber":"09171234567"}
        """;
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    // -----------------------------------------------------------------------
    // TC-AUTH-002: Register with duplicate email → expects 409 with error code
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-AUTH-002: Register with duplicate email returns 409")
    void TC_AUTH_002_register_withDuplicateEmail_returns409() throws Exception {
        String body = """
            {"email":"duplicate@washq.com","password":"pass1234",
             "firstName":"Jane","lastName":"Doe"}
        """;
        // First registration — should succeed
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        // Second registration with same email — should fail with 409
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    // -----------------------------------------------------------------------
    // TC-AUTH-003: Login with valid credentials → expects 200, returns JWT
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-AUTH-003: Login with valid credentials returns 200 and JWT")
    void TC_AUTH_003_login_withValidCredentials_returns200() throws Exception {
        // Pre-condition: register the user
        String registerBody = """
            {"email":"tc003login@washq.com","password":"pass1234",
             "firstName":"Login","lastName":"User"}
        """;
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isCreated());

        // Now login
        String loginBody = """
            {"email":"tc003login@washq.com","password":"pass1234"}
        """;
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    // -----------------------------------------------------------------------
    // TC-AUTH-004: Login with wrong password → expects 401 with error code
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-AUTH-004: Login with wrong password returns 401")
    void TC_AUTH_004_login_withWrongPassword_returns401() throws Exception {
        // Pre-condition: register user
        String registerBody = """
            {"email":"tc004wrong@washq.com","password":"correctPass1",
             "firstName":"Wrong","lastName":"Pass"}
        """;
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isCreated());

        // Login with wrong password
        String loginBody = """
            {"email":"tc004wrong@washq.com","password":"wrongPassword"}
        """;
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    // -----------------------------------------------------------------------
    // TC-AUTH-005: Register with missing/blank fields → expects 400
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-AUTH-005: Register with missing required fields returns 400")
    void TC_AUTH_005_register_withMissingFields_returns400() throws Exception {
        // Missing firstName, lastName, and password too short
        String body = """
            {"email":"tc005missing@washq.com","password":""}
        """;
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }
}
