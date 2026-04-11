package edu.cit.abel.washq.controller;

import edu.cit.abel.washq.dto.ApiResponse;
import edu.cit.abel.washq.dto.TimeSlotDTO;
import edu.cit.abel.washq.service.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    /**
     * GET /api/v1/slots?date=YYYY-MM-DD
     * Authenticated — returns all slots for the requested date.
     * The client decides which are bookable based on currentBookingCount vs maxCapacity.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TimeSlotDTO> slots = timeSlotService.getSlotsByDate(date);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }
}
