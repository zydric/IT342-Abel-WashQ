package edu.cit.abel.washq;

import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for all WashQ integration tests.
 *
 * Activates the "test" Spring profile which loads application-test.properties,
 * switching the datasource to H2 in-memory instead of the Supabase PostgreSQL
 * connection defined in the main application.properties.
 *
 * The "test" profile file (application-test.properties) is loaded AFTER the
 * main application.properties and takes precedence over all datasource settings.
 */
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    // All WashQ integration tests extend this to get H2 in-memory database.
}
