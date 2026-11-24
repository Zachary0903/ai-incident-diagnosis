package com.incidentlab.order.model;

/** Result of pricing one order line: the unit price charged, the discount applied and the total. */
public record PriceQuote(long unitPriceCents, int percentOff, long totalCents) {}
