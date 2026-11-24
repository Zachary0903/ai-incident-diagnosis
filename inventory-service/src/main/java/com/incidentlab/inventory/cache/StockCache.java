package com.incidentlab.inventory.cache;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Redis cache of stock levels (key {@code stock:<sku>}, 30s TTL), evicted on every reservation. */
@Component
public class StockCache {

    private static final Duration TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redis;

    public StockCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public Optional<Long> get(String sku) {
        return Optional.ofNullable(redis.opsForValue().get(key(sku))).map(Long::parseLong);
    }

    public void put(String sku, long stock) {
        redis.opsForValue().set(key(sku), String.valueOf(stock), TTL);
    }

    public void evict(String sku) {
        redis.delete(key(sku));
    }

    private static String key(String sku) {
        return "stock:" + sku;
    }
}
