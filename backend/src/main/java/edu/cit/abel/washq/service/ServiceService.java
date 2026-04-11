package edu.cit.abel.washq.service;

import edu.cit.abel.washq.dto.ServiceDTO;
import edu.cit.abel.washq.dto.ServiceRequestDTO;
import edu.cit.abel.washq.entity.Service;
import edu.cit.abel.washq.repository.ServiceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Returns all services for the admin dashboard.
     */
    public List<ServiceDTO> getAllServices() {
        return serviceRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceDTO createService(ServiceRequestDTO request) {
        Service service = new Service();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPricePerKg(request.getPricePerKg());
        service.setEstimatedDurationHours(request.getEstimatedDurationHours());
        service.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        service = serviceRepository.save(service);
        return toDTO(service);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceDTO updateService(Long id, ServiceRequestDTO request) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPricePerKg(request.getPricePerKg());
        service.setEstimatedDurationHours(request.getEstimatedDurationHours());
        if (request.getIsActive() != null) {
            service.setIsActive(request.getIsActive());
        }
        
        service = serviceRepository.save(service);
        return toDTO(service);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        service.setIsActive(false);
        serviceRepository.save(service);
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
