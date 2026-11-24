package com.incidentlab.inventory.service;

import java.util.Optional;

import com.incidentlab.inventory.cache.StockCache;
import com.incidentlab.inventory.entity.StockMovement;
import com.incidentlab.inventory.gateway.WarehouseGateway;
import com.incidentlab.inventory.model.Reservation;
import com.incidentlab.inventory.model.StockLevel;
import com.incidentlab.inventory.repository.ProductRepository;
import com.incidentlab.inventory.repository.StockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/** Stock levels and reservations. */
@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository products;
    private final StockMovementRepository movements;
    private final StockCache cache;
    private final WarehouseGateway warehouse;
    private final TransactionTemplate tx;

    public InventoryService(ProductRepository products, StockMovementRepository movements, StockCache cache,
                            WarehouseGateway warehouse, TransactionTemplate tx) {
        this.products = products;
        this.movements = movements;
        this.cache = cache;
        this.warehouse = warehouse;
        this.tx = tx;
    }

    /** Cache-aside read of the current stock; empty if the SKU does not exist. */
    public Optional<StockLevel> getStock(String sku) {
        Optional<Long> cached = cache.get(sku);
        if (cached.isPresent()) {
            return Optional.of(new StockLevel(sku, cached.get(), true));
        }
        return products.findById(sku).map(product -> {
            cache.put(sku, product.getStock());
            return new StockLevel(sku, product.getStock(), false);
        });
    }

    /**
     * Reserves {@code qty} units: decrements stock and records the movement in one transaction,
     * then (after commit) evicts the cached stock and confirms with the warehouse.
     *
     * @return empty if the SKU does not exist or has insufficient stock (nothing is changed)
     */
    public Optional<Reservation> reserve(String sku, int qty) {
        Optional<Reservation> reservation = tx.execute(status -> reserveInTransaction(sku, qty));
        if (reservation == null || reservation.isEmpty()) {
            log.warn("Reservation rejected: sku {} not found or insufficient stock for qty {}", sku, qty);
            return Optional.empty();
        }

        // After commit, so neither call holds the row lock or a DB connection (F05 makes confirm() slow).
        cache.evict(sku);
        warehouse.confirm(sku, qty);

        log.info("Reserved {} x{} (historical movements: {})", sku, qty, reservation.get().historicalMovements());
        return reservation;
    }

    private Optional<Reservation> reserveInTransaction(String sku, int qty) {
        // Velocity check runs before taking the row lock, so a slow scan (F01) doesn't block other reservations.
        long historicalMovements = movements.countBySku(sku);

        boolean reserved = products.findWithLockBySku(sku)
                .map(product -> product.reserve(qty))   // dirty entity is flushed on commit
                .orElse(false);
        if (!reserved) {
            return Optional.empty();
        }

        movements.save(new StockMovement(sku, -qty));
        return Optional.of(new Reservation(sku, qty, historicalMovements));
    }
}
