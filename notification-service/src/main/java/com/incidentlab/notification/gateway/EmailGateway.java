package com.incidentlab.notification.gateway;

import com.incidentlab.common.fault.FaultRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stand-in for an external email provider.
 *
 * <p>Fault F03 ({@code slow-email-provider}): each send takes ~1.5s, so the consumer
 * falls behind the ~5 msg/s produced by order-service and lag builds on {@code order-created}.
 */
@Component
public class EmailGateway {

    static final String FAULT_SLOW_EMAIL = "slow-email-provider";

    private final FaultRegistry faults;
    private final long injectedLatencyMs;

    public EmailGateway(FaultRegistry faults, @Value("${email.injected-latency-ms}") long injectedLatencyMs) {
        this.faults = faults;
        this.injectedLatencyMs = injectedLatencyMs;
        faults.register(FAULT_SLOW_EMAIL);
    }

    public void sendOrderConfirmation(long orderId) {
        long latency = faults.isActive(FAULT_SLOW_EMAIL) ? injectedLatencyMs : 20;
        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
