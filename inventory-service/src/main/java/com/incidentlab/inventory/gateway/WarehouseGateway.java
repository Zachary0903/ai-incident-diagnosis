package com.incidentlab.inventory.gateway;

import com.incidentlab.common.fault.FaultRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stand-in for an external warehouse-management API that confirms each reservation.
 *
 * <p>Fault F05 ({@code inventory-latency}): the "warehouse API" becomes slow, so
 * {@code /inventory/reserve} takes longer than order-service's 2s read timeout.
 */
@Component
public class WarehouseGateway {

    static final String FAULT_LATENCY = "inventory-latency";

    private final FaultRegistry faults;
    private final long injectedLatencyMs;

    public WarehouseGateway(FaultRegistry faults, @Value("${warehouse.injected-latency-ms}") long injectedLatencyMs) {
        this.faults = faults;
        this.injectedLatencyMs = injectedLatencyMs;
        faults.register(FAULT_LATENCY);
    }

    public void confirm(String sku, int qty) {
        long latency = faults.isActive(FAULT_LATENCY) ? injectedLatencyMs : 15;
        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
