package edu.cit.abel.washq.service;

import edu.cit.abel.washq.dto.TimeSlotDTO;
import edu.cit.abel.washq.entity.TimeSlot;
import edu.cit.abel.washq.repository.TimeSlotRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TimeSlotService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    /**
     * Returns all non-disabled slots for the requested date, in chronological order.
     * Fully booked slots are included so the UI can render them as greyed-out.
     */
    public List<TimeSlotDTO> getSlotsByDate(LocalDate date) {
        return timeSlotRepository
                .findBySlotDateAndIsAvailableTrueOrderByStartTimeAsc(date)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- Helpers ---

    private TimeSlotDTO toDTO(TimeSlot slot) {
        return new TimeSlotDTO(
                slot.getId(),
                slot.getSlotDate().format(DATE_FMT),
                slot.getStartTime().format(TIME_FMT),
                slot.getEndTime().format(TIME_FMT),
                slot.getMaxCapacity(),
                slot.getCurrentBookingCount(),
                slot.getIsAvailable()
        );
    }
}
