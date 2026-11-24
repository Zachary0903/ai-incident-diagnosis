package com.incidentlab.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.incidentlab.common.fault.FaultRegistry;
import com.incidentlab.order.client.InventoryClient;
import com.incidentlab.order.dto.ProductResponse;
import com.incidentlab.order.exception.InvalidOrderException;
import com.incidentlab.order.exception.UnknownProductException;
import com.incidentlab.order.model.PriceQuote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

    private final InventoryClient inventory = mock(InventoryClient.class);
    private final FaultRegistry faults = new FaultRegistry();
    private final PricingService pricing = new PricingService(inventory, faults);

    @BeforeEach
    void catalog() {
        when(inventory.getProduct("SKU-0001")).thenReturn(new ProductResponse("SKU-0001", "Product 1", 1999));
        when(inventory.getProduct("SKU-0002")).thenReturn(new ProductResponse("SKU-0002", "Product 2", 49999));
        when(inventory.getProduct("SKU-9999")).thenThrow(new UnknownProductException("SKU-9999"));
    }

    @Test
    void usesEachProductsOwnPrice() {
        assertThat(pricing.quote("SKU-0001", 3, null)).isEqualTo(new PriceQuote(1999, 0, 5997));
        assertThat(pricing.quote("SKU-0002", 1, null)).isEqualTo(new PriceQuote(49999, 0, 49999));
    }

    @Test
    void appliesPromoToSubtotal() {
        // 2 x 49999 = 99998, minus 10% = 89998.2 -> truncated to 89998
        assertThat(pricing.quote("SKU-0002", 2, "WELCOME10")).isEqualTo(new PriceQuote(49999, 10, 89998));
    }

    @Test
    void unknownPromoIsIgnoredWhenPricingV2IsOff() {
        assertThat(pricing.quote("SKU-0001", 1, "BLACKFRIDAY").percentOff()).isZero();
    }

    @Test
    void usesWhateverPriceTheCatalogCurrentlyHas() {
        when(inventory.getProduct("SKU-0001")).thenReturn(new ProductResponse("SKU-0001", "Product 1", 2499));

        assertThat(pricing.quote("SKU-0001", 1, null).unitPriceCents()).isEqualTo(2499);
    }

    @Test
    void propagatesUnknownSku() {
        assertThatThrownBy(() -> pricing.quote("SKU-9999", 1, null))
                .isInstanceOf(UnknownProductException.class);
    }

    @Test
    void rejectsNonPositiveQtyWithoutCallingInventory() {
        InventoryClient untouched = mock(InventoryClient.class);
        PricingService service = new PricingService(untouched, faults);

        assertThatThrownBy(() -> service.quote("SKU-0001", 0, null))
                .isInstanceOf(InvalidOrderException.class);
        verifyNoInteractions(untouched);
    }

    @Test
    void faultF04StillThrowsNpeFromPercentOffV2() {
        faults.set(PricingService.FAULT_PROMO_NPE, true);

        assertThatThrownBy(() -> pricing.quote("SKU-0001", 1, "BLACKFRIDAY"))
                .isInstanceOf(NullPointerException.class)
                .satisfies(e -> assertThat(e.getStackTrace())
                        .anyMatch(f -> f.getMethodName().equals("percentOffV2")));
    }
}
