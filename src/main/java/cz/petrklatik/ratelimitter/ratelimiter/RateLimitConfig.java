package cz.petrklatik.ratelimitter.ratelimiter;

/**
 * Per-client rate limit configuration.
 */
public record RateLimitConfig(
    int capacity,
    int refillPerSecond
) {
  /**
   * Default configuration for clients without custom config.
   */
  public static final RateLimitConfig DEFAULT = new RateLimitConfig(100, 10);

  /**
   * Compact constructor with validation.
   */
  public RateLimitConfig {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must be > 0");
    }
    if (refillPerSecond <= 0) {
      throw new IllegalArgumentException("refillPerSecond must be > 0");
    }
  }
}
