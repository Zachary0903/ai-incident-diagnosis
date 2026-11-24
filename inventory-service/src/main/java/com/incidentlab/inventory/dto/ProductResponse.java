package com.incidentlab.inventory.dto;

/** Response of {@code GET /products/{sku}}: catalog data order-service prices orders from. */
public record ProductResponse(String sku, String name, long priceCents) {}
