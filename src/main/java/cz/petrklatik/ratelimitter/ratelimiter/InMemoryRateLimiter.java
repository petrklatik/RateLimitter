package cz.petrklatik.ratelimitter.ratelimiter;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter using ConcurrentHashMap.
 */
@Slf4j
public class InMemoryRateLimiter implements RateLimiter {

  private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, RateLimitConfig> configs = new ConcurrentHashMap<>();
  private final RateLimitConfig defaultConfig;

  public InMemoryRateLimiter(RateLimitConfig defaultConfig) {
    this.defaultConfig = defaultConfig;
  }

  @Override
  public RateLimitResult tryAcquire(String clientId) {
    // computeIfAbsent is atomic - only one bucket created per client
    TokenBucket bucket = buckets.computeIfAbsent(clientId, this::createBucket);
    return bucket.tryConsume();
  }

  @Override
  public void setClientConfig(String clientId, RateLimitConfig config) {
    log.info("Updating config for client '{}': capacity={}, refill={}/s",
        clientId, config.capacity(), config.refillPerSecond());
    configs.put(clientId, config);
    // Preserve current fill ratio when updating config
    buckets.compute(clientId, (_, oldBucket) -> {
      double fillRatio = (oldBucket != null) ? oldBucket.getFillRatio() : 1.0;
      return new TokenBucket(config, fillRatio);
    });
  }

  @Override
  public RateLimitConfig getClientConfig(String clientId) {
    return configs.getOrDefault(clientId, defaultConfig);
  }

  private TokenBucket createBucket(String clientId) {
    log.info("New bucket for client: {}", clientId);
    RateLimitConfig config = configs.getOrDefault(clientId, defaultConfig);
    return new TokenBucket(config);
  }
}
