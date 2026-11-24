package com.incidentlab.notification.service;

import com.incidentlab.notification.gateway.EmailGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Sends customer notifications for order events. */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailGateway email;

    public NotificationService(EmailGateway email) {
        this.email = email;
    }

    /** Blocks for as long as the email provider takes (~1.5s per send under fault F03). */
    public void sendOrderConfirmation(long orderId) {
        long start = System.nanoTime();
        email.sendOrderConfirmation(orderId);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        log.info("Order confirmation sent for order {} in {} ms", orderId, elapsedMs);
    }
}
