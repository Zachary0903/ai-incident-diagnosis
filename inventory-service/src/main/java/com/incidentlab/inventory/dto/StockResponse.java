package com.incidentlab.inventory.dto;

/** Response of {@code GET /inventory/{sku}}. */
public record StockResponse(String sku, long stock, boolean cached) {}
