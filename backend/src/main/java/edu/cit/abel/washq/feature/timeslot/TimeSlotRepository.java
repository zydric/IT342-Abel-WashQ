package edu.cit.abel.washq.feature.timeslot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    /**
     * Returns all time slots for a given date where the slot is manually enabled,
     * ordered by start time ascending.
     */
    List<TimeSlot> findBySlotDateAndIsAvailableTrueOrderByStartTimeAsc(LocalDate slotDate);

    /**
     * Count slots from a given date onwards (used by DataSeeder).
     */
    long countBySlotDateGreaterThanEqual(LocalDate date);
}
