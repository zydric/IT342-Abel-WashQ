package edu.cit.abel.washq.feature.timeslot;

import edu.cit.abel.washq.shared.dto.ApiResponse;
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
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TimeSlotDTO> slots = timeSlotService.getSlotsByDate(date);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }
}
