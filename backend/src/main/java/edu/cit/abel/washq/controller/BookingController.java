package edu.cit.abel.washq.controller;

import edu.cit.abel.washq.dto.ApiResponse;
import edu.cit.abel.washq.dto.BookingRequestDTO;
import edu.cit.abel.washq.dto.BookingResponseDTO;
import edu.cit.abel.washq.exception.BookingException;
import edu.cit.abel.washq.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * POST /api/v1/bookings
     * Create a new booking for the authenticated customer.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(
            Authentication authentication,
            @Valid @RequestBody BookingRequestDTO request) {

        String email = authentication.getName();

        try {
            BookingResponseDTO response = bookingService.createBooking(email, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
        } catch (BookingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getErrorCode(), e.getMessage(), null));
        }
    }
}
