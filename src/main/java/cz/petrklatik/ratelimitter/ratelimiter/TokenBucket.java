package cz.petrklatik.ratelimitter.ratelimiter;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Token Bucket implementation using CAS (Compare-And-Swap).
 */
public class TokenBucket {

  /**
   * Immutable state snapshot for atomic updates.
   */
  private record State(double tokens, long lastRefillNanos) {
  }

  private final AtomicReference<State> state;
  private final int capacity;
  private final int refillPerSecond;

  public TokenBucket(RateLimitConfig config) {
    this(config, 1.0); // Start with full bucket
  }

  public TokenBucket(RateLimitConfig config, double fillRatio) {
    this.capacity = config.capacity();
    this.refillPerSecond = config.refillPerSecond();
    double initialTokens = capacity * Math.max(0.0, Math.min(1.0, fillRatio));
    this.state = new AtomicReference<>(new State(initialTokens, System.nanoTime()));
  }

  /**
   * Returns current fill ratio (0.0 to 1.0) for preserving state during config updates.
   */
  public double getFillRatio() {
    State current = state.get();
    long now = System.nanoTime();
    double elapsedSeconds = (now - current.lastRefillNanos) / 1_000_000_000.0;
    double refilled = current.tokens + elapsedSeconds * refillPerSecond;
    double currentTokens = Math.min(capacity, refilled);
    return currentTokens / capacity;
  }

  /**
   * Attempts to consume one token from the bucket.
   *
   * @return result with allowed status and rate limit info
   */
  public RateLimitResult tryConsume() {
    while (true) {
      State current = state.get();
      long now = System.nanoTime();

      // Calculate tokens to add based on elapsed time
      double elapsedSeconds = (now - current.lastRefillNanos) / 1_000_000_000.0;
      double refilled = current.tokens + elapsedSeconds * refillPerSecond;
      double newTokens = Math.min(capacity, refilled);

      if (newTokens >= 1.0) {
        // Consume one token
        State next = new State(newTokens - 1.0, now);
        if (state.compareAndSet(current, next)) {
          return RateLimitResult.allowed((int) next.tokens, capacity);
        }
        // Another thread modified state, retry
      } else {
        // Not enough tokens - calculate wait time
        double tokensNeeded = 1.0 - newTokens;
        long waitMillis = (long) Math.ceil((tokensNeeded / refillPerSecond) * 1000);

        // Update timestamp even on rejection
        State next = new State(newTokens, now);
        if (state.compareAndSet(current, next)) {
          return RateLimitResult.rejected(waitMillis, capacity);
        }
        // retry
      }
    }
  }
}
