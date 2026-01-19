package cz.petrklatik.ratelimitter.infrastructure;

import cz.petrklatik.ratelimitter.ratelimiter.InMemoryRateLimiter;
import cz.petrklatik.ratelimitter.ratelimiter.RateLimitConfig;
import cz.petrklatik.ratelimitter.ratelimiter.RateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("RateLimitFilter Integration")
class RateLimitFilterIntegrationTest {

  private static final String CLIENT_ID_HEADER = "X-Client-Id";
  private static final String RATE_LIMIT_LIMIT_HEADER = "X-RateLimit-Limit";
  private static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
  private static final String RETRY_AFTER_HEADER = "Retry-After";
  private static final String API_TEST_ENDPOINT = "/api/test";

  @TestConfiguration
  static class TestConfig {
    @Bean
    @Primary
    RateLimiter testRateLimiter() {
      return new InMemoryRateLimiter(new RateLimitConfig(5, 1));
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("should return 200 with rate limit headers when under limit")
  void shouldReturn200WhenUnderLimit() throws Exception {
    // Given
    String clientId = "under-limit-client";

    // When
    ResultActions result = mockMvc.perform(get(API_TEST_ENDPOINT)
        .header(CLIENT_ID_HEADER, clientId));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(header().exists(RATE_LIMIT_LIMIT_HEADER))
        .andExpect(header().exists(RATE_LIMIT_REMAINING_HEADER));
  }

  @Test
  @DisplayName("should return 429 with Retry-After header when limit exceeded")
  void shouldReturn429WhenLimitExceeded() throws Exception {
    // Given
    String clientId = "over-limit-client";
    for (int i = 0; i < 5; i++) {
      mockMvc.perform(get(API_TEST_ENDPOINT).header(CLIENT_ID_HEADER, clientId))
          .andExpect(status().isOk());
    }

    // When
    ResultActions result = mockMvc.perform(get(API_TEST_ENDPOINT)
        .header(CLIENT_ID_HEADER, clientId));

    // Then
    result
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists(RETRY_AFTER_HEADER))
        .andExpect(header().string(RATE_LIMIT_REMAINING_HEADER, "0"));
  }

  @Test
  @DisplayName("should return 400 when X-Client-Id header is missing")
  void shouldReturn400WhenMissingClientId() throws Exception {
    // Given - no client ID header

    // When
    ResultActions result = mockMvc.perform(get(API_TEST_ENDPOINT));

    // Then
    result
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("application/problem+json"));
  }

  @Test
  @DisplayName("should return correct rate limit headers")
  void shouldReturnCorrectHeaders() throws Exception {
    // Given
    String clientId = "header-test-client";

    // When
    ResultActions result = mockMvc.perform(get(API_TEST_ENDPOINT)
        .header(CLIENT_ID_HEADER, clientId));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(header().string(RATE_LIMIT_LIMIT_HEADER, "5"))
        .andExpect(header().string(RATE_LIMIT_REMAINING_HEADER, "4"));
  }

  @Test
  @DisplayName("should decrement remaining count on each request")
  void shouldDecrementRemainingOnEachRequest() throws Exception {
    // Given
    String clientId = "decrement-client";

    // When
    List<ResultActions> results = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      results.add(mockMvc.perform(get(API_TEST_ENDPOINT)
          .header(CLIENT_ID_HEADER, clientId)));
    }

    // Then
    results.get(0).andExpect(header().string(RATE_LIMIT_REMAINING_HEADER, "4"));
    results.get(1).andExpect(header().string(RATE_LIMIT_REMAINING_HEADER, "3"));
    results.get(2).andExpect(header().string(RATE_LIMIT_REMAINING_HEADER, "2"));
  }

  @Test
  @DisplayName("should isolate rate limits between different clients")
  void shouldIsolateClientLimits() throws Exception {
    // Given
    String clientA = "isolated-A";
    String clientB = "isolated-B";

    // When - Client A exhausts limit
    for (int i = 0; i < 5; i++) {
      mockMvc.perform(get(API_TEST_ENDPOINT).header(CLIENT_ID_HEADER, clientA))
          .andExpect(status().isOk());
    }
    ResultActions clientAExhausted = mockMvc.perform(get(API_TEST_ENDPOINT)
        .header(CLIENT_ID_HEADER, clientA));

    // When - Client B makes request
    ResultActions clientBResult = mockMvc.perform(get(API_TEST_ENDPOINT)
        .header(CLIENT_ID_HEADER, clientB));

    // Then
    clientAExhausted.andExpect(status().isTooManyRequests());
    clientBResult.andExpect(status().isOk());
  }
}
