package edu.cit.abel.washq.service;

import edu.cit.abel.washq.dto.*;
import edu.cit.abel.washq.entity.*;
import edu.cit.abel.washq.exception.BookingException;
import edu.cit.abel.washq.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

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

        // Validate double-booking (SLOT-002)
        if (bookingRepository.existsByUserAndTimeSlot(user, timeSlot)) {
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
        BookingResponseDTO response = new BookingResponseDTO();
        response.setId(booking.getId());
        response.setEstimatedWeightKg(booking.getEstimatedWeightKg());
        response.setSpecialInstructions(booking.getSpecialInstructions());
        response.setStatus(booking.getStatus());
        response.setTotalAmount(booking.getTotalAmount());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        
        // Mock PayMongo URL for now, real integration usually happens via external API 
        response.setPaymentUrl("https://paymongo.com/checkout/mock_url_" + booking.getId());

        // Partial DTO mapping for relationships to keep JSON clean
        response.setService(new ServiceDTO(
                service.getId(), service.getName(), service.getDescription(), 
                service.getPricePerKg(), service.getEstimatedDurationHours(), service.getIsActive()
        ));
        
        response.setTimeSlot(new TimeSlotDTO(
                timeSlot.getId(), timeSlot.getSlotDate().format(DATE_FMT), 
                timeSlot.getStartTime().format(TIME_FMT), timeSlot.getEndTime().format(TIME_FMT),
                timeSlot.getMaxCapacity(), timeSlot.getCurrentBookingCount(), timeSlot.getIsAvailable()
        ));

        return response;
    }
}
