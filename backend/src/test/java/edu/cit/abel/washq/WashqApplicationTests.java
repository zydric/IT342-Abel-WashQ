package edu.cit.abel.washq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * WashqApplicationTests — TC-APP-001
 * Verifies the Spring application context loads successfully
 * using H2 in-memory database (via BaseIntegrationTest overrides).
 */
@SpringBootTest
class WashqApplicationTests extends BaseIntegrationTest {

    // TC-APP-001: Spring application context loads without errors
    @Test
    @DisplayName("TC-APP-001: Spring application context loads successfully")
    void contextLoads() {
        // If this test passes, the Spring context initializes correctly
        // with all beans, JPA entities, security config, and feature slices.
    }
}
