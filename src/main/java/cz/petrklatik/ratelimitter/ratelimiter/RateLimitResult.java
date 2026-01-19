package cz.petrklatik.ratelimitter.ratelimiter;

/**
 * Immutable result of a rate limit check.
 */
public record RateLimitResult(
    boolean allowed,
    int remaining,
    int limit,
    long retryAfterMillis
) {
  /**
   * Factory method for allowed requests.
   */
  public static RateLimitResult allowed(int remaining, int limit) {
    return new RateLimitResult(true, remaining, limit, 0);
  }

  /**
   * Factory method for rejected requests.
   */
  public static RateLimitResult rejected(long retryAfterMillis, int limit) {
    return new RateLimitResult(false, 0, limit, retryAfterMillis);
  }

  /**
   * Converts retry time to seconds (for Retry-After header).
   */
  public long retryAfterSeconds() {
    return (long) Math.ceil(retryAfterMillis / 1000.0);
  }
}
