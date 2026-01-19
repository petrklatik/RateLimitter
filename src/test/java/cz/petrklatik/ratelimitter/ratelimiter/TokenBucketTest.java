package cz.petrklatik.ratelimitter.ratelimiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenBucket")
class TokenBucketTest {

  @Test
  @DisplayName("should allow requests up to capacity then deny")
  void shouldAllowRequestsUpToCapacity() {
    // Given
    var config = new RateLimitConfig(10, 1);
    var bucket = new TokenBucket(config);

    // When
    List<RateLimitResult> results = new ArrayList<>();
    for (int i = 0; i < 11; i++) {
      results.add(bucket.tryConsume());
    }

    // Then
    assertThat(results.subList(0, 10)).allMatch(RateLimitResult::allowed);
    assertThat(results.get(10).allowed()).isFalse();
  }

  @Test
  @DisplayName("should return correct remaining count after each request")
  void shouldReturnCorrectRemainingCount() {
    // Given
    var config = new RateLimitConfig(5, 1);
    var bucket = new TokenBucket(config);

    // When
    List<RateLimitResult> results = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      results.add(bucket.tryConsume());
    }

    // Then
    assertThat(results.get(0).remaining()).isEqualTo(4);
    assertThat(results.get(1).remaining()).isEqualTo(3);
    assertThat(results.get(2).remaining()).isEqualTo(2);
    assertThat(results.get(3).remaining()).isEqualTo(1);
    assertThat(results.get(4).remaining()).isEqualTo(0);
  }

  @Test
  @DisplayName("should return retry-after value when request is denied")
  void shouldReturnRetryAfterWhenDenied() {
    // Given
    var config = new RateLimitConfig(1, 1);
    var bucket = new TokenBucket(config);
    bucket.tryConsume(); // exhaust the single token

    // When
    RateLimitResult result = bucket.tryConsume();

    // Then
    assertThat(result.allowed()).isFalse();
    assertThat(result.retryAfterMillis()).isPositive();
  }

  @Test
  @DisplayName("should refill tokens over time")
  void shouldRefillTokensOverTime() throws InterruptedException {
    // Given
    var config = new RateLimitConfig(10, 10); // 10 tokens/sec
    var bucket = new TokenBucket(config);
    for (int i = 0; i < 10; i++) {
      bucket.tryConsume(); // exhaust all tokens
    }
    assertThat(bucket.tryConsume().allowed()).isFalse();

    // When
    Thread.sleep(500); // Wait 0.5s -> ~5 tokens refilled
    List<RateLimitResult> results = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      results.add(bucket.tryConsume());
    }

    // Then
    long allowedCount = results.stream().filter(RateLimitResult::allowed).count();
    assertThat(allowedCount)
        .as("Expected ~5 tokens refilled after 500ms")
        .isBetween(4L, 6L);
  }

  @Test
  @DisplayName("should start with full bucket")
  void shouldStartWithFullBucket() {
    // Given
    var config = new RateLimitConfig(100, 10);

    // When
    var bucket = new TokenBucket(config);

    // Then
    assertThat(bucket.getFillRatio()).isCloseTo(1.0, org.assertj.core.api.Assertions.within(0.01));
  }

  @Test
  @DisplayName("should preserve fill ratio when created with initial ratio")
  void shouldPreserveFillRatioOnCreation() {
    // Given
    var config = new RateLimitConfig(100, 10);

    // When
    var bucket = new TokenBucket(config, 0.5); // 50% full
    List<RateLimitResult> results = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      results.add(bucket.tryConsume());
    }

    // Then
    long allowedCount = results.stream().filter(RateLimitResult::allowed).count();
    assertThat(allowedCount)
        .as("Expected ~50 tokens (50%% fill ratio)")
        .isBetween(49L, 51L);
  }

  @Test
  @DisplayName("should report correct limit from config")
  void shouldReportCorrectLimit() {
    // Given
    var config = new RateLimitConfig(42, 10);
    var bucket = new TokenBucket(config);

    // When
    RateLimitResult result = bucket.tryConsume();

    // Then
    assertThat(result.limit()).isEqualTo(42);
  }
}
