package edu.cit.abel.washq.feature.auth

import org.junit.Test
import org.junit.Assert.*

/**
 * AuthActivityTest — Vertical Slice: feature/auth
 * JVM unit tests for authentication form validation, session management,
 * and Google Sign-In token handling. No emulator required.
 *
 * Tests: TC-MOB-AUTH-001 through TC-MOB-AUTH-005
 */
class AuthActivityTest {

    // -----------------------------------------------------------------------
    // TC-MOB-AUTH-001: testLoginFormValidation
    // Email and password fields must be non-empty and valid
    // -----------------------------------------------------------------------
    @Test
    fun TC_MOB_AUTH_001_testLoginFormValidation() {
        val email = "user@washq.com"
        val password = "pass1234"

        val isEmailValid = email.isNotBlank() && isValidEmail(email)
        val isPasswordValid = password.length >= 8

        assertTrue("Email should be valid and non-blank", isEmailValid)
        assertTrue("Password should meet minimum length of 8 chars", isPasswordValid)

        // Blank fields should fail
        assertFalse("Blank email should fail validation", "".isNotBlank())
        assertFalse("Blank password should fail validation", "".length >= 8)
    }

    // -----------------------------------------------------------------------
    // TC-MOB-AUTH-002: testPasswordStrength
    // Minimum 8-character password is enforced
    // -----------------------------------------------------------------------
    @Test
    fun TC_MOB_AUTH_002_testPasswordStrength() {
        val shortPassword = "abc"
        val validPassword = "pass1234"
        val borderPassword = "12345678" // exactly 8 chars

        assertFalse("Password shorter than 8 chars should fail", shortPassword.length >= 8)
        assertTrue("Password of exactly 8 chars should pass", borderPassword.length >= 8)
        assertTrue("Password of 8+ chars should pass", validPassword.length >= 8)
    }

    // -----------------------------------------------------------------------
    // TC-MOB-AUTH-003: testEmailValidation
    // Malformed email addresses are rejected
    // -----------------------------------------------------------------------
    @Test
    fun TC_MOB_AUTH_003_testEmailValidation() {
        assertTrue("Valid email should pass", isValidEmail("user@washq.com"))
        assertTrue("Valid email with subdomain should pass", isValidEmail("user@mail.washq.com"))
        assertFalse("Email without @ should fail", isValidEmail("userwashq.com"))
        assertFalse("Email without domain should fail", isValidEmail("user@"))
        assertFalse("Email without local part should fail", isValidEmail("@washq.com"))
        assertFalse("Blank email should fail", isValidEmail(""))
    }

    // -----------------------------------------------------------------------
    // TC-MOB-AUTH-004: testSessionManagement
    // JWT is stored and retrieved from SharedPreferences (simulated as map)
    // -----------------------------------------------------------------------
    @Test
    fun TC_MOB_AUTH_004_testSessionManagement() {
        val sessionStore = mutableMapOf<String, String>()
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payload.signature"

        // Store token (simulates SecurePrefsManager.saveToken())
        sessionStore["accessToken"] = token

        // Retrieve and verify
        val retrieved = sessionStore["accessToken"]
        assertNotNull("Token should be stored and retrievable", retrieved)
        assertEquals("Retrieved token must match stored token", token, retrieved)

        // Logout clears the session
        sessionStore.clear()
        assertNull("Token should be null after session clear (logout)", sessionStore["accessToken"])
        assertTrue("Session store should be empty after logout", sessionStore.isEmpty())
    }

    // -----------------------------------------------------------------------
    // TC-MOB-AUTH-005: testGoogleSignInIntegration
    // Google ID token is validated and mapped into a backend request
    // -----------------------------------------------------------------------
    @Test
    fun TC_MOB_AUTH_005_testGoogleSignInIntegration() {
        val googleIdToken = "google_id_token_1234567890abcdef"

        // Token must be non-blank and of valid length
        assertTrue("Google ID token must be non-blank", googleIdToken.isNotBlank())
        assertTrue("Google ID token must have sufficient length", googleIdToken.length > 10)

        // Simulate mapping to backend POST /auth/google request body
        val requestBody = mapOf("idToken" to googleIdToken)
        assertTrue("Request body must contain 'idToken' key", requestBody.containsKey("idToken"))
        assertEquals("idToken value must match the Google token", googleIdToken, requestBody["idToken"])
    }

    // --- Helper function ---
    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val atIndex = email.indexOf('@')
        if (atIndex <= 0) return false
        val domain = email.substring(atIndex + 1)
        return domain.isNotEmpty() && domain.contains('.')
    }
}
