package com.incidentlab.inventory.dto;

/** Error body, e.g. the 409 of {@code POST /inventory/reserve}. */
public record ErrorResponse(String error, String sku) {}
