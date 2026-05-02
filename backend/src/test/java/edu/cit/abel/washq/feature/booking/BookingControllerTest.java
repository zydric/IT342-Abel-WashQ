package edu.cit.abel.washq.feature.booking;

import edu.cit.abel.washq.feature.catalog.WashService;
import edu.cit.abel.washq.feature.catalog.ServiceRepository;
import edu.cit.abel.washq.feature.timeslot.TimeSlot;
import edu.cit.abel.washq.feature.timeslot.TimeSlotRepository;
import edu.cit.abel.washq.feature.user.User;
import edu.cit.abel.washq.feature.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    private Long validServiceId;
    private Long validTimeSlotId;

    @BeforeEach
    void setup() {
        // Setup mock user in database matching the @WithMockUser email
        if (!userRepository.existsByEmail("customer@washq.com")) {
            User user = new User();
            user.setEmail("customer@washq.com");
            user.setFirstName("Cust");
            user.setLastName("Omer");
            user.setRole("CUSTOMER");
            userRepository.save(user);
        }

        // Setup a service
        WashService service = new WashService();
        service.setName("Basic Wash");
        service.setPricePerKg(new BigDecimal("50.0"));
        service.setEstimatedDurationHours(2);
        service.setIsActive(true);
        service = serviceRepository.save(service);
        validServiceId = service.getId();

        // Setup a time slot
        TimeSlot slot = new TimeSlot();
        slot.setSlotDate(LocalDate.now());
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(10, 0));
        slot.setMaxCapacity(5);
        slot.setCurrentBookingCount(0);
        slot.setIsAvailable(true);
        slot = timeSlotRepository.save(slot);
        validTimeSlotId = slot.getId();
    }

    @Test
    @WithMockUser(username = "customer@washq.com", roles = "CUSTOMER")
    void createBooking_withValidSlot_returns201() throws Exception {
        String body = String.format("""
            {"serviceId":%d,"timeSlotId":%d,"estimatedWeightKg":5.0,
             "specialInstructions":"Fold please"}
        """, validServiceId, validTimeSlotId);
        
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "customer@washq.com", roles = "CUSTOMER")
    void createBooking_sameSlot_returnsConflict() throws Exception {
        String body = String.format("""
            {"serviceId":%d,"timeSlotId":%d,"estimatedWeightKg":5.0,
             "specialInstructions":"Fold please"}
        """, validServiceId, validTimeSlotId);
        
        // First booking — success
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        // Second booking on the same slot — expect CONFLICT
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("SLOT-002"));
    }

    @Test
    void getBookings_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/bookings"))
            .andExpect(status().isUnauthorized()); // Or 403 depending on spring security config
    }

    @Test
    @WithMockUser(username = "customer@washq.com", roles = "CUSTOMER")
    void createService_asCustomer_returns403() throws Exception {
        String body = """
            {"name":"Premium Wash","pricePerKg":100.0,"estimatedDurationHours":3}
        """;
        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }
}
