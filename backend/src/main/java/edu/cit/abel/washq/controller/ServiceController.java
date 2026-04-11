package edu.cit.abel.washq.controller;

import edu.cit.abel.washq.dto.ApiResponse;
import edu.cit.abel.washq.dto.ServiceDTO;
import edu.cit.abel.washq.service.ServiceService;
import org.springframework.http.ResponseEntity;
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
     * Public endpoint — returns all active laundry services.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices() {
        List<ServiceDTO> services = serviceService.getAllActiveServices();
        return ResponseEntity.ok(ApiResponse.success(services));
    }
}
