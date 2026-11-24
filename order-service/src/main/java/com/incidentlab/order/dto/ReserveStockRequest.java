package com.incidentlab.order.dto;

/** Body sent to inventory-service's {@code POST /inventory/reserve}. */
public record ReserveStockRequest(String sku, int qty) {}
