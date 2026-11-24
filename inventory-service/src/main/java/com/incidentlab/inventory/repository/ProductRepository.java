package com.incidentlab.inventory.repository;

import java.util.Optional;

import com.incidentlab.inventory.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ProductRepository extends JpaRepository<Product, String> {

    /**
     * Loads the product with a pessimistic write lock ({@code SELECT ... FOR UPDATE}), so concurrent
     * reservations of the same SKU queue up instead of overwriting each other's stock update.
     * Must be called inside a transaction; the lock is released on commit.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findWithLockBySku(String sku);
}
