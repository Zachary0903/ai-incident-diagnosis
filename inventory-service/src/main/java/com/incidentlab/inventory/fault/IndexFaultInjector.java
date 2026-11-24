package com.incidentlab.inventory.fault;

import com.incidentlab.common.fault.FaultRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Fault F01 ({@code missing-index}): drops {@code idx_sku} on the ~2M-row stock_movement
 * table, turning the velocity check in
 * {@link com.incidentlab.inventory.service.InventoryService#reserve} into a full scan.
 * Disabling recreates the index (takes a few seconds).
 *
 * <p>The one place in this service that still uses SQL: dropping/creating an index is DDL,
 * which JPA has no API for. It simulates a schema migration, not application data access.
 */
@Component
public class IndexFaultInjector {

    static final String FAULT_MISSING_INDEX = "missing-index";

    private final JdbcTemplate jdbc;

    public IndexFaultInjector(FaultRegistry faults, JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        faults.register(FAULT_MISSING_INDEX, this::apply);
    }

    private void apply(boolean enabled) {
        boolean exists = indexExists();
        if (enabled && exists) {
            jdbc.execute("ALTER TABLE stock_movement DROP INDEX idx_sku");
        } else if (!enabled && !exists) {
            jdbc.execute("CREATE INDEX idx_sku ON stock_movement (sku)");
        }
    }

    private boolean indexExists() {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.statistics
                WHERE table_schema = DATABASE() AND table_name = 'stock_movement' AND index_name = 'idx_sku'
                """, Integer.class);
        return count != null && count > 0;
    }
}
