package cz.petrklatik.ratelimitter.ratelimiter.distributed;

import cz.petrklatik.ratelimitter.ratelimiter.RateLimitConfig;
import cz.petrklatik.ratelimitter.ratelimiter.RateLimitResult;
import cz.petrklatik.ratelimitter.ratelimiter.RateLimiter;

// Redis-based Rate Limiter for distributed deployments
public class RedisRateLimiter implements RateLimiter {

  // private final ClientRateLimitRepository repository;
  // private final StringRedisTemplate redis;
  private final RateLimitConfig defaultConfig;

  public RedisRateLimiter(RateLimitConfig defaultConfig) {
    this.defaultConfig = defaultConfig;
  }

  // private final RedisScript<List<Long>> script; // loaded from resources/scripts/token_bucket.lua

  @Override
  public RateLimitResult tryAcquire(String clientId) {
    // RateLimitConfig config = getClientConfig(clientId);
    // String key = "ratelimit:" + clientId;
    //
    // List<Long> result = redis.execute(script,
    //     List.of(key),
    //     String.valueOf(config.capacity()),
    //     String.valueOf(config.refillPerSecond()),
    //     String.valueOf(System.currentTimeMillis()));
    //
    // boolean allowed = result.get(0) == 1;
    // int remaining = result.get(1).intValue();
    // long retryAfterMs = result.get(2);
    //
    // return allowed
    //     ? RateLimitResult.allowed(remaining, config.capacity())
    //     : RateLimitResult.rejected(retryAfterMs, config.capacity());
    throw new UnsupportedOperationException("Redis not configured");
  }

  // @Cacheable("clientConfigs")
  @Override
  public RateLimitConfig getClientConfig(String clientId) {
    // return repository.findById(clientId).map(...).orElse(defaultConfig);
    return defaultConfig;
  }

  // @CacheEvict(value = "clientConfigs", key = "#clientId")
  @Override
  public void setClientConfig(String clientId, RateLimitConfig config) {
    // repository.save(new ClientRateLimitEntity(clientId, config));
    throw new UnsupportedOperationException("Redis not configured");
  }
}
