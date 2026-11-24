package com.incidentlab.order.service;

import java.util.Map;

import com.incidentlab.common.fault.FaultRegistry;
import com.incidentlab.order.client.InventoryClient;
import com.incidentlab.order.exception.InvalidOrderException;
import com.incidentlab.order.exception.UnknownProductException;
import com.incidentlab.order.model.PriceQuote;
import org.springframework.stereotype.Service;

/**
 * Computes order totals from the product's current list price, owned by inventory-service.
 *
 * <p>Fault F04 ({@code promo-npe}): switches on "pricing v2", which forgets to handle
 * unknown promo codes and throws a NullPointerException for them.
 */
@Service
public class PricingService {

    static final String FAULT_PROMO_NPE = "promo-npe";

    private record Promo(String code, int percentOff) {}

    private static final Map<String, Promo> PROMOS = Map.of(
            "WELCOME10", new Promo("WELCOME10", 10),
            "VIP20", new Promo("VIP20", 20));

    private final InventoryClient inventory;
    private final FaultRegistry faults;

    public PricingService(InventoryClient inventory, FaultRegistry faults) {
        this.inventory = inventory;
        this.faults = faults;
        faults.register(FAULT_PROMO_NPE);
    }

    /**
     * Prices {@code qty} units of {@code sku} at its current list price, then applies the promo.
     * Called before reserving stock, so a pricing failure never leaves a reservation behind.
     *
     * @throws InvalidOrderException   if {@code qty} is not positive
     * @throws UnknownProductException if the SKU is not in the catalog
     */
    public PriceQuote quote(String sku, int qty, String promoCode) {
        if (qty <= 0) {
            throw new InvalidOrderException("qty must be positive, was " + qty);
        }
        long unitPriceCents = inventory.getProduct(sku).priceCents();

        long subtotal = Math.multiplyExact(unitPriceCents, qty);
        int percentOff = faults.isActive(FAULT_PROMO_NPE)
                ? percentOffV2(promoCode)
                : percentOffV1(promoCode);
        long totalCents = subtotal * (100 - percentOff) / 100;
        return new PriceQuote(unitPriceCents, percentOff, totalCents);
    }

    private int percentOffV1(String promoCode) {
        if (promoCode == null) {
            return 0;
        }
        Promo promo = PROMOS.get(promoCode);
        return promo == null ? 0 : promo.percentOff();
    }

    /** "Refactored" lookup: drops the null check for unknown codes. */
    private int percentOffV2(String promoCode) {
        if (promoCode == null || promoCode.isBlank()) {
            return 0;
        }
        Promo promo = lookupPromo(promoCode);
        return promo.percentOff();
    }

    private Promo lookupPromo(String code) {
        return PROMOS.get(code.trim().toUpperCase());
    }
}
