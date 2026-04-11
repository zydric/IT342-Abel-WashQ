package edu.cit.abel.washq.repository;

import edu.cit.abel.washq.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    /** Returns only active services for the customer-facing catalog. */
    List<Service> findByIsActiveTrue();
}
