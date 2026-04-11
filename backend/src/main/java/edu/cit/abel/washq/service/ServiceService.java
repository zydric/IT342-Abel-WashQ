package edu.cit.abel.washq.service;

import edu.cit.abel.washq.dto.ServiceDTO;
import edu.cit.abel.washq.entity.Service;
import edu.cit.abel.washq.repository.ServiceRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    /**
     * Returns all active services for the public catalog.
     */
    public List<ServiceDTO> getAllActiveServices() {
        return serviceRepository.findByIsActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- Helpers ---

    private ServiceDTO toDTO(Service s) {
        return new ServiceDTO(
                s.getId(),
                s.getName(),
                s.getDescription(),
                s.getPricePerKg(),
                s.getEstimatedDurationHours(),
                s.getIsActive()
        );
    }
}
