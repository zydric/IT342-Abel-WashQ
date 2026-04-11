package edu.cit.abel.washq.repository;

import edu.cit.abel.washq.entity.Booking;
import edu.cit.abel.washq.entity.TimeSlot;
import edu.cit.abel.washq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Checks if an active booking already exists for a specific user and time slot.
     */
    boolean existsByUserAndTimeSlotAndStatusNot(User user, TimeSlot timeSlot, String status);

    /**
     * Retrieves all bookings for a user ordered by newest first.
     */
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
}
