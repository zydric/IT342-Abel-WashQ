package edu.cit.abel.washq.service;

import edu.cit.abel.washq.dto.*;
import edu.cit.abel.washq.entity.*;
import edu.cit.abel.washq.exception.BookingException;
import edu.cit.abel.washq.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final TimeSlotRepository timeSlotRepository;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          ServiceRepository serviceRepository,
                          TimeSlotRepository timeSlotRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional
    public BookingResponseDTO createBooking(String userEmail, BookingRequestDTO request) {
        // Find entities
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        edu.cit.abel.washq.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Time Slot not found"));

        // Validate double-booking (SLOT-002) - Ignore CANCELLED bookings so users can re-book
        if (bookingRepository.existsByUserAndTimeSlotAndStatusNot(user, timeSlot, "CANCELLED")) {
            throw new BookingException("SLOT-002", "User already has a booking in this time slot");
        }

        // Validate capacity (SLOT-001)
        if (timeSlot.getCurrentBookingCount() >= timeSlot.getMaxCapacity()) {
            throw new BookingException("SLOT-001", "The selected time slot has reached its maximum booking capacity");
        }

        // Calculate total amount
        BigDecimal totalAmount = service.getPricePerKg().multiply(request.getEstimatedWeightKg());

        // Increment time slot booking count
        timeSlot.setCurrentBookingCount(timeSlot.getCurrentBookingCount() + 1);
        timeSlotRepository.save(timeSlot);

        // Create booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setService(service);
        booking.setTimeSlot(timeSlot);
        booking.setEstimatedWeightKg(request.getEstimatedWeightKg());
        booking.setSpecialInstructions(request.getSpecialInstructions());
        booking.setTotalAmount(totalAmount);
        
        // Save booking
        booking = bookingRepository.save(booking);

        // Map to ResponseDTO
        BookingResponseDTO response = mapToDTO(booking);
        
        // Mock PayMongo URL for now, real integration usually happens via external API 
        response.setPaymentUrl("https://paymongo.com/checkout/mock_url_" + booking.getId());

        return response;
    }

    public List<BookingResponseDTO> getUserBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<Booking> bookings = bookingRepository.findByUserOrderByCreatedAtDesc(user);
        
        return bookings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void cancelBooking(String userEmail, Long bookingId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
                
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to cancel this booking");
        }
        
        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("Only PENDING bookings can be cancelled");
        }
        
        booking.setStatus("CANCELLED");
        
        // Release slot count
        TimeSlot slot = booking.getTimeSlot();
        if (slot.getCurrentBookingCount() > 0) {
            slot.setCurrentBookingCount(slot.getCurrentBookingCount() - 1);
            timeSlotRepository.save(slot);
        }
        
        bookingRepository.save(booking);
    }
    
    private BookingResponseDTO mapToDTO(Booking booking) {
        BookingResponseDTO response = new BookingResponseDTO();
        response.setId(booking.getId());
        response.setEstimatedWeightKg(booking.getEstimatedWeightKg());
        response.setSpecialInstructions(booking.getSpecialInstructions());
        response.setStatus(booking.getStatus());
        response.setTotalAmount(booking.getTotalAmount());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        
        edu.cit.abel.washq.entity.Service service = booking.getService();
        if (service != null) {
            response.setService(new ServiceDTO(
                    service.getId(), service.getName(), service.getDescription(), 
                    service.getPricePerKg(), service.getEstimatedDurationHours(), service.getIsActive()
            ));
        }
        
        TimeSlot timeSlot = booking.getTimeSlot();
        if (timeSlot != null) {
            response.setTimeSlot(new TimeSlotDTO(
                    timeSlot.getId(), timeSlot.getSlotDate().format(DATE_FMT), 
                    timeSlot.getStartTime().format(TIME_FMT), timeSlot.getEndTime().format(TIME_FMT),
                    timeSlot.getMaxCapacity(), timeSlot.getCurrentBookingCount(), timeSlot.getIsAvailable()
            ));
        }
        
        return response;
    }
}
