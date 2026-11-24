package com.incidentlab.inventory.model;

/** Current stock of a SKU, and whether it was served from the Redis cache. */
public record StockLevel(String sku, long stock, boolean cached) {}
