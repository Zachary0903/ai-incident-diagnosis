package com.incidentlab.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.incidentlab.inventory.cache.StockCache;
import com.incidentlab.inventory.entity.Product;
import com.incidentlab.inventory.gateway.WarehouseGateway;
import com.incidentlab.inventory.repository.ProductRepository;
import com.incidentlab.inventory.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Real commits from parallel threads: the row lock must stop concurrent reservations from overselling. */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")  // main config says "none"
@Import(InventoryService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class InventoryServiceConcurrencyTest {

    @Autowired InventoryService inventory;
    @Autowired ProductRepository products;
    @Autowired StockMovementRepository movements;
    @MockitoBean StockCache cache;
    @MockitoBean WarehouseGateway warehouse;

    @Test
    void parallelReservationsNeverOversell() throws Exception {
        products.save(new Product("SKU-HOT", "Hot product", 1999, 10));

        List<Callable<Boolean>> buyers = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            buyers.add(() -> inventory.reserve("SKU-HOT", 1).isPresent());
        }
        ExecutorService pool = Executors.newFixedThreadPool(8);
        long succeeded;
        try {
            succeeded = pool.invokeAll(buyers).stream().filter(InventoryServiceConcurrencyTest::get).count();
        } finally {
            pool.shutdown();
        }

        assertThat(succeeded).isEqualTo(10);
        assertThat(products.findById("SKU-HOT").orElseThrow().getStock()).isZero();
        assertThat(movements.countBySku("SKU-HOT")).isEqualTo(10);
    }

    private static boolean get(Future<Boolean> f) {
        try {
            return f.get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
