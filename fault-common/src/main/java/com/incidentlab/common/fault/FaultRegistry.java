package com.incidentlab.common.fault;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

/**
 * Holds which injected faults are currently active in this service.
 *
 * <p>Deliberately does NOT log fault names: everything this service logs is exported to Loki,
 * and the diagnosis agent must find root causes from symptoms, not from a "fault enabled" line.
 * Ground truth lives only in {@code scenarios/*.yaml}.
 */
@Component
public class FaultRegistry {

    private final Map<String, Boolean> active = new ConcurrentHashMap<>();
    private final Map<String, Consumer<Boolean>> hooks = new ConcurrentHashMap<>();

    /** Declares a fault this service supports. */
    public void register(String id) {
        active.putIfAbsent(id, false);
    }

    /** Declares a fault with a hook that runs on every toggle (true = enabled). */
    public void register(String id, Consumer<Boolean> hook) {
        register(id);
        hooks.put(id, hook);
    }

    public boolean isActive(String id) {
        return active.getOrDefault(id, false);
    }

    public boolean isKnown(String id) {
        return active.containsKey(id);
    }

    public Set<String> known() {
        return active.keySet();
    }

    public Map<String, Boolean> snapshot() {
        return new TreeMap<>(active);
    }

    public void set(String id, boolean enabled) {
        if (!isKnown(id)) {
            throw new IllegalArgumentException("Unknown fault: " + id);
        }
        Consumer<Boolean> hook = hooks.get(id);
        if (hook != null) {
            hook.accept(enabled);
        }
        active.put(id, enabled);
    }
}
