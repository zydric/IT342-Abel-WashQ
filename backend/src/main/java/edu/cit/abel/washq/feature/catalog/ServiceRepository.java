package edu.cit.abel.washq.feature.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<WashService, Long> {

    /** Returns only active services for the customer-facing catalog. */
    List<WashService> findByIsActiveTrue();
}
