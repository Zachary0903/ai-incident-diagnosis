package com.incidentlab.inventory.repository;

import com.incidentlab.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /** Velocity check. Fast only while index {@code idx_sku} exists (fault F01 drops it). */
    long countBySku(String sku);
}
