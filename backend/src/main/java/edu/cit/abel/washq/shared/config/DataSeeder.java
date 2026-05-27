package edu.cit.abel.washq.shared.config;

import edu.cit.abel.washq.feature.timeslot.TimeSlot;
import edu.cit.abel.washq.feature.timeslot.TimeSlotRepository;
import edu.cit.abel.washq.feature.user.User;
import edu.cit.abel.washq.feature.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds time_slots and basic admin/staff roles on startup if empty or missing.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(TimeSlotRepository timeSlotRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Admin and Staff accounts
        seedSystemAccounts();

        // 2. Seed Time Slots
        seedTimeSlots();
    }

    private void seedSystemAccounts() {
        System.out.println("🌱 Cleaning up and seeding Admin & Staff accounts...");

        // Remove old entries if they exist to avoid duplication and allow password reset
        userRepository.findByEmail("admin@washq.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("staff@washq.com").ifPresent(userRepository::delete);

        // Create Admin
        User admin = new User();
        admin.setEmail("admin@washq.com");
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setPasswordHash(passwordEncoder.encode("password123"));
        admin.setRole("ADMIN");
        admin.setAddress("WashQ HQ, Cebu City");
        admin.setContactNumber("09171234567");
        userRepository.save(admin);

        // Create Staff
        User staff = new User();
        staff.setEmail("staff@washq.com");
        staff.setFirstName("Jane");
        staff.setLastName("Staff");
        staff.setPasswordHash(passwordEncoder.encode("password123"));
        staff.setRole("STAFF");
        staff.setAddress("WashQ Hub, Cebu City");
        staff.setContactNumber("09187654321");
        userRepository.save(staff);

        System.out.println("✅ Seeded Admin and Staff accounts successfully (Password: password123).");
    }

    private void seedTimeSlots() {
        LocalDate today = LocalDate.now();
        long futureSlots = timeSlotRepository.countBySlotDateGreaterThanEqual(today);
        if (futureSlots > 0) {
            System.out.println("✅ Time slots already exist (" + futureSlots + " future slots). Skipping seeder.");
            return;
        }

        System.out.println("🌱 Seeding time slots for the next 14 days...");

        List<TimeSlot> slots = new ArrayList<>();
        for (int day = 0; day < 14; day++) {
            LocalDate date = today.plusDays(day);

            // 7 slots per day: 08:00-10:00, 10:00-12:00, 12:00-14:00, 14:00-16:00,
            //                   16:00-18:00, 18:00-20:00, 20:00-22:00
            for (int hour = 8; hour <= 20; hour += 2) {
                TimeSlot slot = new TimeSlot();
                slot.setSlotDate(date);
                slot.setStartTime(LocalTime.of(hour, 0));
                slot.setEndTime(LocalTime.of(hour + 2, 0));
                slot.setMaxCapacity(5);
                slot.setCurrentBookingCount(0);
                slot.setIsAvailable(true);
                slots.add(slot);
            }
        }

        timeSlotRepository.saveAll(slots);
        System.out.println("✅ Seeded " + slots.size() + " time slots (14 days × 7 slots/day).");
    }
}
