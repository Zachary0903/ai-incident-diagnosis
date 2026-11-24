package com.incidentlab.inventory.model;

/** A successful stock reservation; {@code historicalMovements} is the velocity-check result. */
public record Reservation(String sku, int qty, long historicalMovements) {}
