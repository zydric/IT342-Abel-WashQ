package edu.cit.abel.washq.feature.booking;

import edu.cit.abel.washq.BaseIntegrationTest;
import edu.cit.abel.washq.feature.catalog.WashService;
import edu.cit.abel.washq.feature.catalog.ServiceRepository;
import edu.cit.abel.washq.feature.timeslot.TimeSlot;
import edu.cit.abel.washq.feature.timeslot.TimeSlotRepository;
import edu.cit.abel.washq.feature.user.User;
import edu.cit.abel.washq.feature.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BookingControllerTest — Vertical Slice: feature/booking
 * Tests all booking endpoints using MockMvc + H2 in-memory database.
 * Extends BaseIntegrationTest to ensure H2 overrides Supabase PostgreSQL settings.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Long validServiceId;
    private Long validTimeSlotId;

    @BeforeEach
    void setup() {
        // Ensure a CUSTOMER user exists matching @WithMockUser email
        if (!userRepository.existsByEmail("customer@washq.com")) {
            User user = new User();
            user.setEmail("customer@washq.com");
            user.setFirstName("Cust");
            user.setLastName("Omer");
            user.setRole("CUSTOMER");
            userRepository.save(user);
        }

        // Create a STAFF user for status-update tests
        if (!userRepository.existsByEmail("staff@washq.com")) {
            User staff = new User();
            staff.setEmail("staff@washq.com");
            staff.setFirstName("Staff");
            staff.setLastName("User");
            staff.setRole("STAFF");
            userRepository.save(staff);
        }

        // Create a laundry service
        WashService service = new WashService();
        service.setName("Basic Wash");
        service.setPricePerKg(new BigDecimal("50.0"));
        service.setEstimatedDurationHours(2);
        service.setIsActive(true);
        service = serviceRepository.save(service);
        validServiceId = service.getId();

        // Create a time slot
        TimeSlot slot = new TimeSlot();
        slot.setSlotDate(LocalDate.now().plusDays(1));
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(10, 0));
        slot.setMaxCapacity(5);
        slot.setCurrentBookingCount(0);
        slot.setIsAvailable(true);
        slot = timeSlotRepository.save(slot);
        validTimeSlotId = slot.getId();
    }

    // -----------------------------------------------------------------------
    // TC-BOOK-001: Create booking with valid data → expects 201
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-BOOK-001: Create booking with valid data returns 201")
    @WithMockUser(username = "customer@washq.com", roles = "CUSTOMER")
    void TC_BOOK_001_createBooking_withValidData_returns201() throws Exception {
        String body = String.format("""
            {"serviceId":%d,"timeSlotId":%d,"estimatedWeightKg":5.0,
             "specialInstructions":"Please fold neatly"}
        """, validServiceId, validTimeSlotId);

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    // -----------------------------------------------------------------------
    // TC-BOOK-002: Get all bookings for authenticated user → expects 200, array
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-BOOK-002: Get bookings for authenticated customer returns 200")
    @WithMockUser(username = "customer@washq.com", roles = "CUSTOMER")
    void TC_BOOK_002_getBookings_asAuthenticatedCustomer_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/bookings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    // -----------------------------------------------------------------------
    // TC-BOOK-003: Get booking by ID (via create then list) → expects 200
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-BOOK-003: Get bookings list contains booking detail objects")
    @WithMockUser(username = "customer@washq.com", roles = "CUSTOMER")
    void TC_BOOK_003_getBookings_returnsDetailObjects() throws Exception {
        // Create a booking first
        String body = String.format("""
            {"serviceId":%d,"timeSlotId":%d,"estimatedWeightKg":3.0,
             "specialInstructions":"Quick wash"}
        """, validServiceId, validTimeSlotId);

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        // Then retrieve the list — expect at least one element with id field
        mockMvc.perform(get("/api/v1/bookings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists())
            .andExpect(jsonPath("$.data[0].status").exists());
    }

    // -----------------------------------------------------------------------
    // TC-BOOK-004: Access bookings without JWT → expects 401 or 403
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-BOOK-004: Access bookings without JWT returns 401 or 403")
    void TC_BOOK_004_getBookings_withoutJwt_returns401() throws Exception {
        // Spring Security may return 401 (Unauthorized) or 403 (Forbidden) for
        // unauthenticated requests depending on the ExceptionTranslationFilter config.
        mockMvc.perform(get("/api/v1/bookings"))
            .andExpect(result ->
                org.junit.jupiter.api.Assertions.assertTrue(
                    result.getResponse().getStatus() == 401 ||
                    result.getResponse().getStatus() == 403,
                    "Expected 401 or 403 for unauthenticated request but got: " +
                    result.getResponse().getStatus()
                )
            );
    }

    // -----------------------------------------------------------------------
    // TC-BOOK-005: Update booking status (STAFF) → expects 200
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-BOOK-005: STAFF can update booking status to RECEIVED")
    @WithMockUser(username = "staff@washq.com", roles = "STAFF")
    void TC_BOOK_005_updateBookingStatus_asStaff_returns200() throws Exception {
        // Create a booking as CUSTOMER first using repository directly
        User customer = userRepository.findByEmail("customer@washq.com").orElseThrow();
        WashService service = serviceRepository.findById(validServiceId).orElseThrow();
        TimeSlot slot = timeSlotRepository.findById(validTimeSlotId).orElseThrow();

        Booking booking = new Booking();
        booking.setUser(customer);
        booking.setService(service);
        booking.setTimeSlot(slot);
        booking.setEstimatedWeightKg(new BigDecimal("4.0"));
        booking.setTotalAmount(new BigDecimal("200.0"));
        booking = bookingRepository.save(booking);

        String statusBody = "{\"status\":\"RECEIVED\"}";

        mockMvc.perform(patch("/api/v1/bookings/" + booking.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("RECEIVED"));
    }

    // -----------------------------------------------------------------------
    // TC-BOOK-006: Cancel (delete) a PENDING booking → expects 200
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-BOOK-006: Cancel PENDING booking returns 200")
    @WithMockUser(username = "customer@washq.com", roles = "CUSTOMER")
    void TC_BOOK_006_cancelBooking_asPendingCustomer_returns200() throws Exception {
        // Create a booking first
        String createBody = String.format("""
            {"serviceId":%d,"timeSlotId":%d,"estimatedWeightKg":2.5,
             "specialInstructions":""}
        """, validServiceId, validTimeSlotId);

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isCreated());

        // Get its ID via repository
        User customer = userRepository.findByEmail("customer@washq.com").orElseThrow();
        Booking booking = bookingRepository.findByUserOrderByCreatedAtDesc(customer).get(0);

        mockMvc.perform(delete("/api/v1/bookings/" + booking.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // -----------------------------------------------------------------------
    // TC-BOOK-007: Duplicate booking on same slot → 409 CONFLICT (SLOT-002)
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-BOOK-007: Duplicate booking on same slot returns 409")
    @WithMockUser(username = "customer@washq.com", roles = "CUSTOMER")
    void TC_BOOK_007_createBooking_sameSlot_returnsConflict() throws Exception {
        String body = String.format("""
            {"serviceId":%d,"timeSlotId":%d,"estimatedWeightKg":5.0,
             "specialInstructions":"Fold please"}
        """, validServiceId, validTimeSlotId);

        // First booking — success
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        // Second on same slot — conflict
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("SLOT-002"));
    }

    // -----------------------------------------------------------------------
    // TC-BOOK-008: Verify catalog endpoint is accessible to authenticated users
    //              but ADMIN-only operations are enforced via @PreAuthorize
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-BOOK-008: Unauthenticated POST /api/v1/services returns 401 or 403")
    void TC_BOOK_008_createService_withoutJwt_returnsProtected() throws Exception {
        String body = """
            {"name":"Unauthorized Wash","pricePerKg":50.0,"estimatedDurationHours":1}
        """;
        // Without authentication, endpoint should be protected
        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(result ->
                org.junit.jupiter.api.Assertions.assertTrue(
                    result.getResponse().getStatus() == 401 ||
                    result.getResponse().getStatus() == 403,
                    "Expected 401 or 403 for unauthenticated service creation but got: " +
                    result.getResponse().getStatus()
                )
            );
    }
}
