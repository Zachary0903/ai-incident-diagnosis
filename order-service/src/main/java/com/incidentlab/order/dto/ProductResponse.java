package com.incidentlab.order.dto;

/** inventory-service's answer to {@code GET /products/{sku}}. */
public record ProductResponse(String sku, String name, long priceCents) {}
