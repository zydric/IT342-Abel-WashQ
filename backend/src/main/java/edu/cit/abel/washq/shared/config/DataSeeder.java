package edu.cit.abel.washq.shared.config;

import edu.cit.abel.washq.feature.timeslot.TimeSlot;
import edu.cit.abel.washq.feature.timeslot.TimeSlotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds time_slots table on startup if empty.
 * Generates 7 slots/day for the next 14 days (08:00–22:00, 2-hour windows).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final TimeSlotRepository timeSlotRepository;

    public DataSeeder(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @Override
    public void run(String... args) {
        // Only seed if no future slots exist
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
