package edu.cit.abel.washq.feature.payment;

import edu.cit.abel.washq.feature.booking.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);
    Optional<Payment> findByPaymongoCheckoutId(String checkoutId);
    boolean existsByBooking(Booking booking);
}
