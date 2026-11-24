package com.incidentlab.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.incidentlab.inventory.cache.StockCache;
import com.incidentlab.inventory.entity.Product;
import com.incidentlab.inventory.entity.StockMovement;
import com.incidentlab.inventory.gateway.WarehouseGateway;
import com.incidentlab.inventory.model.Reservation;
import com.incidentlab.inventory.model.StockLevel;
import com.incidentlab.inventory.repository.ProductRepository;
import com.incidentlab.inventory.repository.StockMovementRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")  // main config says "none"
@Import(InventoryService.class)
class InventoryServiceTest {

    @Autowired InventoryService inventory;
    @Autowired ProductRepository products;
    @Autowired StockMovementRepository movements;
    @Autowired EntityManager em;
    @MockitoBean StockCache cache;
    @MockitoBean WarehouseGateway warehouse;

    @BeforeEach
    void seed() {
        products.save(new Product("SKU-0001", "Product 1", 1999, 5));
        movements.save(new StockMovement("SKU-0001", 50));
        movements.save(new StockMovement("SKU-0001", -1));
        movements.save(new StockMovement("SKU-0002", -1));
    }

    @Test
    void reservesStockRecordsMovementThenEvictsCacheAndConfirms() {
        assertThat(inventory.reserve("SKU-0001", 2)).contains(new Reservation("SKU-0001", 2, 2));

        em.flush();
        em.clear();
        assertThat(products.findById("SKU-0001").orElseThrow().getStock()).isEqualTo(3);
        assertThat(movements.countBySku("SKU-0001")).isEqualTo(3);
        verify(cache).evict("SKU-0001");
        verify(warehouse).confirm("SKU-0001", 2);
    }

    @Test
    void insufficientStockChangesNothing() {
        assertThat(inventory.reserve("SKU-0001", 6)).isEmpty();

        em.flush();
        em.clear();
        assertThat(products.findById("SKU-0001").orElseThrow().getStock()).isEqualTo(5);
        assertThat(movements.countBySku("SKU-0001")).isEqualTo(2);
        verify(cache, never()).evict(any());
        verify(warehouse, never()).confirm(any(), anyInt());
    }

    @Test
    void canReserveExactlyTheRemainingStock() {
        assertThat(inventory.reserve("SKU-0001", 5)).isPresent();
        assertThat(inventory.reserve("SKU-0001", 1)).isEmpty();
    }

    @Test
    void unknownSkuIsRejected() {
        assertThat(inventory.reserve("NOPE", 1)).isEmpty();
    }

    @Test
    void getStockServesCacheHit() {
        when(cache.get("SKU-0001")).thenReturn(Optional.of(42L));

        assertThat(inventory.getStock("SKU-0001")).contains(new StockLevel("SKU-0001", 42, true));
    }

    @Test
    void getStockMissReadsDatabaseAndFillsCache() {
        when(cache.get("SKU-0001")).thenReturn(Optional.empty());

        assertThat(inventory.getStock("SKU-0001")).contains(new StockLevel("SKU-0001", 5, false));
        verify(cache).put("SKU-0001", 5);
    }

    @Test
    void getStockUnknownSkuIsEmptyAndNotCached() {
        when(cache.get("NOPE")).thenReturn(Optional.empty());

        assertThat(inventory.getStock("NOPE")).isEmpty();
        verify(cache, never()).put(any(), anyLong());
    }
}
