package cz.petrklatik.ratelimitter.ratelimiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InMemoryRateLimiter")
class InMemoryRateLimiterTest {

  private static final RateLimitConfig DEFAULT_CONFIG = new RateLimitConfig(10, 1);
  private InMemoryRateLimiter rateLimiter;

  @BeforeEach
  void setUp() {
    rateLimiter = new InMemoryRateLimiter(DEFAULT_CONFIG);
  }

  @Test
  @DisplayName("should isolate rate limits between different clients")
  void shouldIsolateClients() {
    // Given
    String clientA = "client-A";
    String clientB = "client-B";

    // When - Client A exhausts their limit
    List<RateLimitResult> clientAResults = new ArrayList<>();
    for (int i = 0; i < 11; i++) {
      clientAResults.add(rateLimiter.tryAcquire(clientA));
    }

    // When - Client B makes requests
    List<RateLimitResult> clientBResults = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      clientBResults.add(rateLimiter.tryAcquire(clientB));
    }

    // Then
    assertThat(clientAResults.subList(0, 10)).allMatch(RateLimitResult::allowed);
    assertThat(clientAResults.get(10).allowed()).isFalse();
    assertThat(clientBResults).allMatch(RateLimitResult::allowed);
  }

  @Test
  @DisplayName("should apply default config for new clients")
  void shouldApplyDefaultConfig() {
    // Given
    String newClient = "new-client";

    // When
    RateLimitResult result = rateLimiter.tryAcquire(newClient);

    // Then
    assertThat(result.limit()).isEqualTo(10);
  }

  @Test
  @DisplayName("should apply custom config for specific client")
  void shouldApplyCustomConfig() {
    // Given
    String premiumClient = "premium";
    var customConfig = new RateLimitConfig(100, 10);
    rateLimiter.setClientConfig(premiumClient, customConfig);

    // When
    RateLimitResult result = rateLimiter.tryAcquire(premiumClient);

    // Then
    assertThat(result.limit()).isEqualTo(100);
  }

  @Test
  @DisplayName("should preserve fill ratio when updating client config")
  void shouldPreserveFillRatioOnConfigUpdate() {
    // Given
    String client = "client";
    for (int i = 0; i < 5; i++) {
      rateLimiter.tryAcquire(client); // Consume 5 of 10 tokens (50%)
    }

    // When - Update to 100 capacity (should have ~50 tokens at 50%)
    rateLimiter.setClientConfig(client, new RateLimitConfig(100, 10));
    List<RateLimitResult> results = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      results.add(rateLimiter.tryAcquire(client));
    }

    // Then
    long allowedCount = results.stream().filter(RateLimitResult::allowed).count();
    assertThat(allowedCount)
        .as("Expected ~50 tokens after update (50%% fill ratio preserved)")
        .isBetween(48L, 52L);
  }

  @Test
  @DisplayName("should return custom config for configured client")
  void shouldReturnClientConfig() {
    // Given
    String vipClient = "vip";
    var customConfig = new RateLimitConfig(500, 50);
    rateLimiter.setClientConfig(vipClient, customConfig);

    // When
    RateLimitConfig retrievedConfig = rateLimiter.getClientConfig(vipClient);

    // Then
    assertThat(retrievedConfig).isEqualTo(customConfig);
  }

  @Test
  @DisplayName("should return default config for unknown client")
  void shouldReturnDefaultForUnknownClient() {
    // Given
    String unknownClient = "unknown";

    // When
    RateLimitConfig retrievedConfig = rateLimiter.getClientConfig(unknownClient);

    // Then
    assertThat(retrievedConfig).isEqualTo(DEFAULT_CONFIG);
  }
}
