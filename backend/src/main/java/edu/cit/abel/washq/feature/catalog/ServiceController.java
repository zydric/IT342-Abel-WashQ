package edu.cit.abel.washq.feature.catalog;

import edu.cit.abel.washq.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final CatalogService catalogService;

    public ServiceController(CatalogService catalogService) {
        this.catalogService = catalogService;
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
            services = catalogService.getAllServices();
        } else {
            services = catalogService.getAllActiveServices();
        }
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    /**
     * POST /api/v1/services
     * Create a new service (ADMIN only).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceDTO>> createService(@Valid @RequestBody ServiceRequestDTO request) {
        ServiceDTO response = catalogService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * PUT /api/v1/services/{id}
     * Update an existing service (ADMIN only).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDTO>> updateService(@PathVariable Long id, @Valid @RequestBody ServiceRequestDTO request) {
        try {
            ServiceDTO response = catalogService.updateService(id, request);
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
    public ResponseEntity<ApiResponse<Void>> deactivateService(@PathVariable Long id) {
        try {
            catalogService.deactivateService(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", e.getMessage(), null));
        }
    }
}
