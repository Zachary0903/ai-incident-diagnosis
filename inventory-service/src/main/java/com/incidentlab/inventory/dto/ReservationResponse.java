package com.incidentlab.inventory.dto;

/** 200 response of {@code POST /inventory/reserve}. */
public record ReservationResponse(String sku, int reserved) {}
