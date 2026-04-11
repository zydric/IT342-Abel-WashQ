package edu.cit.abel.washq.repository;

import edu.cit.abel.washq.entity.Booking;
import edu.cit.abel.washq.entity.TimeSlot;
import edu.cit.abel.washq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Checks if a booking already exists for a specific user and time slot.
     */
    boolean existsByUserAndTimeSlot(User user, TimeSlot timeSlot);
}
