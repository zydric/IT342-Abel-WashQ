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
import java.util.List;

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

    /**
     * GET /api/v1/bookings
     * Retrieve all bookings for the authenticated customer.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getBookings(Authentication authentication) {
        String email = authentication.getName();
        boolean isStaffOrAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF") || a.getAuthority().equals("ROLE_ADMIN"));
        
        List<BookingResponseDTO> bookings;
        if (isStaffOrAdmin) {
            bookings = bookingService.getAllBookings();
        } else {
            bookings = bookingService.getUserBookings(email);
        }
        
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    /**
     * PATCH /api/v1/bookings/{id}/status
     * Update booking status (STAFF/ADMIN only).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> updateBookingStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        
        String email = authentication.getName();
        String status = body.get("status");
        
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("BAD_REQUEST", "Status is required", null));
        }

        try {
            BookingResponseDTO response = bookingService.updateBookingStatus(id, status, email);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("BAD_REQUEST", e.getMessage(), null));
        } catch (Exception e) {
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SERVER_ERROR", "Internal error: " + e.getMessage(), null));
        }
    }

    /**
     * DELETE /api/v1/bookings/{id}
     * Cancel a specific booking for the authenticated customer if it is PENDING.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            Authentication authentication,
            @PathVariable Long id) {
        
        String email = authentication.getName();
        try {
            bookingService.cancelBooking(email, id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("BAD_REQUEST", e.getMessage(), null));
        }
    }
}
