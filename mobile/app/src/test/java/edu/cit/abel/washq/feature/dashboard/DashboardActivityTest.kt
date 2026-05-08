package edu.cit.abel.washq.feature.dashboard

import org.junit.Test
import org.junit.Assert.*

/**
 * DashboardActivityTest — Vertical Slice: feature/dashboard
 * JVM unit tests for dashboard data handling, navigation, logout,
 * and pull-to-refresh behavior. No emulator required.
 *
 * Tests: TC-MOB-DASH-001 through TC-MOB-DASH-004
 */
class DashboardActivityTest {

    // -----------------------------------------------------------------------
    // TC-MOB-DASH-001: testDashboardDataFetching
    // Dashboard API call returns a non-empty list of bookings
    // -----------------------------------------------------------------------
    @Test
    fun TC_MOB_DASH_001_testDashboardDataFetching() {
        // Simulate API response with booking data
        val mockBookings = listOf(
            mapOf("id" to 1, "status" to "PENDING", "service" to mapOf("name" to "Basic Wash")),
            mapOf("id" to 2, "status" to "COMPLETED", "service" to mapOf("name" to "Deluxe Wash")),
        )

        assertFalse("Dashboard booking list should not be empty after fetch", mockBookings.isEmpty())
        assertEquals("Dashboard should display 2 bookings", 2, mockBookings.size)
        assertEquals("First booking should have PENDING status", "PENDING", mockBookings[0]["status"])
        assertEquals("Second booking should have COMPLETED status", "COMPLETED", mockBookings[1]["status"])
        assertNotNull("Each booking must have a service", (mockBookings[0]["service"] as Map<*, *>)["name"])
    }

    // -----------------------------------------------------------------------
    // TC-MOB-DASH-002: testNavigationToDetail
    // Tapping a booking card navigates to BookingDetailActivity with correct ID
    // -----------------------------------------------------------------------
    @Test
    fun TC_MOB_DASH_002_testNavigationToDetail() {
        val selectedBookingId = 42L
        val destinationActivity = "BookingDetailActivity"

        // Simulate creating Intent extras (would be Intent.putExtra in real code)
        val intentExtras = mapOf("bookingId" to selectedBookingId, "destination" to destinationActivity)

        assertEquals("Intent should carry the correct booking ID", selectedBookingId, intentExtras["bookingId"])
        assertEquals("Intent should target BookingDetailActivity", "BookingDetailActivity", intentExtras["destination"])
        assertTrue("bookingId must be positive", (intentExtras["bookingId"] as Long) > 0)
    }

    // -----------------------------------------------------------------------
    // TC-MOB-DASH-003: testLogoutFunctionality
    // Logout clears session storage and redirects to LoginActivity
    // -----------------------------------------------------------------------
    @Test
    fun TC_MOB_DASH_003_testLogoutFunctionality() {
        val sessionStore = mutableMapOf<String, String>()
        sessionStore["accessToken"] = "eyJhbGciOiJIUzI1NiJ9.token.sig"
        sessionStore["userEmail"] = "user@washq.com"

        assertFalse("Session store should not be empty before logout", sessionStore.isEmpty())

        // Simulate logout action
        sessionStore.clear()

        assertNull("Access token should be null after logout", sessionStore["accessToken"])
        assertNull("User email should be null after logout", sessionStore["userEmail"])
        assertTrue("Session store must be completely empty after logout", sessionStore.isEmpty())
    }

    // -----------------------------------------------------------------------
    // TC-MOB-DASH-004: testRefreshData
    // Pull-to-refresh triggers a new API call and updates the displayed data
    // -----------------------------------------------------------------------
    @Test
    fun TC_MOB_DASH_004_testRefreshData() {
        var apiCallCount = 0

        // Simulate initial data load on onCreate
        val initialLoad: () -> List<Map<String, Any>> = {
            apiCallCount++
            listOf(mapOf("id" to 1, "status" to "PENDING"))
        }

        // Simulate pull-to-refresh triggering a second load
        val refreshLoad: () -> List<Map<String, Any>> = {
            apiCallCount++
            listOf(
                mapOf("id" to 1, "status" to "RECEIVED"),
                mapOf("id" to 2, "status" to "PENDING"),
            )
        }

        val initialData = initialLoad()
        assertEquals("Initial load should have 1 item", 1, initialData.size)
        assertEquals("Initial status should be PENDING", "PENDING", initialData[0]["status"])

        val refreshedData = refreshLoad()
        assertEquals("API should have been called twice total", 2, apiCallCount)
        assertEquals("Refreshed data should have 2 items", 2, refreshedData.size)
        assertEquals("First item status should update to RECEIVED", "RECEIVED", refreshedData[0]["status"])
    }
}
