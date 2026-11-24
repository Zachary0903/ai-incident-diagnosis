package com.incidentlab.inventory.dto;

/** Body of {@code POST /inventory/reserve}. */
public record ReserveRequest(String sku, int qty) {}
