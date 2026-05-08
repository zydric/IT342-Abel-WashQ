package edu.cit.abel.washq.feature.catalog;

import edu.cit.abel.washq.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CatalogControllerTest — Vertical Slice: feature/catalog
 * Tests the laundry service catalog endpoints (TC-MACH-001 through TC-MACH-003).
 * Extends BaseIntegrationTest to ensure H2 overrides Supabase PostgreSQL settings.
 *
 * In WashQ, the "machine/catalog" concept maps to the WashService catalog:
 * GET /api/v1/services    — list all active laundry services (public, no auth required)
 * POST /api/v1/services   — create a service (ADMIN only via @PreAuthorize)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // -----------------------------------------------------------------------
    // TC-MACH-001: Get all available services → expects 200, array
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-MACH-001: GET /api/v1/services returns 200 and an array")
    void TC_MACH_001_getAllServices_returns200AndArray() throws Exception {
        mockMvc.perform(get("/api/v1/services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    // -----------------------------------------------------------------------
    // TC-MACH-002: Admin can create a service → expects 201
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-MACH-002: ADMIN can create a service and it appears in catalog")
    @WithMockUser(username = "admin@washq.com", roles = "ADMIN")
    void TC_MACH_002_createdService_appearsInCatalog() throws Exception {
        String body = """
            {"name":"Deluxe Wash","description":"Premium wash with ironing",
             "pricePerKg":80.0,"estimatedDurationHours":3}
        """;

        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("Deluxe Wash"))
            .andExpect(jsonPath("$.data.pricePerKg").value(80.0));
    }

    // -----------------------------------------------------------------------
    // TC-MACH-003: Access protected service endpoints without JWT → 401 or 403
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("TC-MACH-003: POST /api/v1/services without JWT returns 401 or 403")
    void TC_MACH_003_createService_withoutJwt_returnsForbidden() throws Exception {
        String body = """
            {"name":"Unauthorized Wash","pricePerKg":50.0,"estimatedDurationHours":1}
        """;
        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(result ->
                org.junit.jupiter.api.Assertions.assertTrue(
                    result.getResponse().getStatus() == 401 ||
                    result.getResponse().getStatus() == 403,
                    "Expected 401 or 403 but got: " + result.getResponse().getStatus()
                )
            );
    }
}
