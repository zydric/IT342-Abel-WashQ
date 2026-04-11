package edu.cit.abel.washq.controller;

import edu.cit.abel.washq.dto.ApiResponse;
import edu.cit.abel.washq.dto.ServiceDTO;
import edu.cit.abel.washq.dto.ServiceRequestDTO;
import edu.cit.abel.washq.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    /**
     * GET /api/v1/services
     * Returns active laundry services for customers, and all services for STAFF/ADMIN.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices(Authentication authentication) {
        boolean isStaffOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF") || a.getAuthority().equals("ROLE_ADMIN"));
        
        List<ServiceDTO> services;
        if (isStaffOrAdmin) {
            services = serviceService.getAllServices();
        } else {
            services = serviceService.getAllActiveServices();
        }
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    /**
     * POST /api/v1/services
     * Create a new service (ADMIN only).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceDTO>> createService(@Valid @RequestBody ServiceRequestDTO request) {
        ServiceDTO response = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * PUT /api/v1/services/{id}
     * Update an existing service (ADMIN only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceDTO>> updateService(@PathVariable Long id, @Valid @RequestBody ServiceRequestDTO request) {
        try {
            ServiceDTO response = serviceService.updateService(id, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", e.getMessage(), null));
        }
    }

    /**
     * DELETE /api/v1/services/{id}
     * Soft-delete (deactivate) a service (ADMIN only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateService(@PathVariable Long id) {
        try {
            serviceService.deactivateService(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", e.getMessage(), null));
        }
    }
}
