package com.incidentlab.common.controller;

import java.util.Map;

import com.incidentlab.common.fault.FaultRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints used by {@code scripts/fault.sh} to switch faults on and off.
 *
 * <p>Note: the OTel agent still records traces for these calls. Agent tools must filter
 * out {@code /admin/} spans, or the answer leaks into the evidence.
 */
@RestController
@RequestMapping("/admin/faults")
public class FaultController {

    /** A logger name that looks like a deploy pipeline, so "change" events read as real ops evidence. */
    private static final Logger deployLog = LoggerFactory.getLogger("deployments");

    private final FaultRegistry registry;
    private final String serviceName;

    public FaultController(FaultRegistry registry, @Value("${spring.application.name}") String serviceName) {
        this.registry = registry;
        this.serviceName = serviceName;
    }

    @GetMapping
    public Map<String, Boolean> list() {
        return registry.snapshot();
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<Map<String, Boolean>> enable(@PathVariable String id,
                                                       @RequestParam(required = false) String change) {
        return toggle(id, true, change);
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<Map<String, Boolean>> disable(@PathVariable String id,
                                                        @RequestParam(required = false) String change) {
        return toggle(id, false, change);
    }

    private ResponseEntity<Map<String, Boolean>> toggle(String id, boolean enabled, String change) {
        if (!registry.isKnown(id)) {
            return ResponseEntity.notFound().build();
        }
        registry.set(id, enabled);
        if (change != null && !change.isBlank()) {
            deployLog.info("Change applied to {}: {}", serviceName, change);
        }
        return ResponseEntity.ok(registry.snapshot());
    }
}
