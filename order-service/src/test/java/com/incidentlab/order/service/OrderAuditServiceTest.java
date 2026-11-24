package com.incidentlab.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import com.incidentlab.common.fault.FaultRegistry;
import com.incidentlab.order.entity.OrderAudit;
import com.incidentlab.order.repository.OrderAuditRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/** Each test leaves the pool in a different state, so each gets a fresh context. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.datasource.url=jdbc:h2:mem:audit;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderAuditServiceTest {

    @Autowired OrderAuditService audit;
    @Autowired OrderAuditRepository audits;
    @Autowired FaultRegistry faults;
    @Autowired DataSource dataSource;

    @Test
    void normalPathWritesRowAndReturnsConnection() throws Exception {
        for (int i = 0; i < 20; i++) {
            audit.record(42, "CREATED");
        }

        assertThat(audits.findByOrderIdOrderByIdAsc(42)).hasSize(20)
                .extracting(OrderAudit::getEvent).containsOnly("CREATED");
        assertThat(activeConnections()).isZero();
    }

    @Test
    void faultF02LeaksOneConnectionPerWrite() throws Exception {
        faults.set(OrderAuditService.FAULT_CONNECTION_LEAK, true);

        for (int i = 0; i < 3; i++) {
            audit.record(7, "CREATED");
        }

        assertThat(audits.findByOrderIdOrderByIdAsc(7)).hasSize(3);  // rows are still written
        assertThat(activeConnections()).isEqualTo(3);                  // but connections never come back

        faults.set(OrderAuditService.FAULT_CONNECTION_LEAK, false);
        assertThat(activeConnections()).isEqualTo(3);                  // turning it off doesn't recover them
    }

    private int activeConnections() throws Exception {
        return dataSource.unwrap(HikariDataSource.class).getHikariPoolMXBean().getActiveConnections();
    }
}
