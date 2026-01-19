package cz.petrklatik.ratelimitter.ratelimiter;

/**
 * Core rate limiting interface.
 */
public interface RateLimiter {

  /**
   * Attempts to acquire a permit for the given client.
   *
   * @param clientId unique identifier for the client
   * @return result containing allowed status, remaining tokens, and retry info
   */
  RateLimitResult tryAcquire(String clientId);

  /**
   * Sets custom rate limit for a specific client (existing bucket is replaced).
   *
   * @param clientId unique client identifier
   * @param config   custom configuration for this client
   */
  void setClientConfig(String clientId, RateLimitConfig config);

  /**
   * Returns current config for client (or default if not set).
   *
   * @param clientId unique client identifier
   * @return client's config or default
   */
  RateLimitConfig getClientConfig(String clientId);
}
