package cz.petrklatik.ratelimitter.ratelimiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Concurrency")
class ConcurrencyTest {

  @Test
  @DisplayName("TokenBucket should handle concurrent requests without race conditions")
  void tokenBucketShouldHandleConcurrentRequests() throws Exception {
    // Given
    int capacity = 100;
    var config = new RateLimitConfig(capacity, 1); // Minimal refill - won't affect test
    var bucket = new TokenBucket(config);

    int threadCount = 50;
    int requestsPerThread = 10;
    int totalRequests = threadCount * requestsPerThread;

    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    AtomicInteger allowedCount = new AtomicInteger(0);
    AtomicInteger deniedCount = new AtomicInteger(0);

    // When - all threads start simultaneously
    List<Future<?>> futures = new ArrayList<>();
    for (int t = 0; t < threadCount; t++) {
      futures.add(executor.submit(() -> {
        try {
          startLatch.await(); // Wait for all threads to be ready
          for (int r = 0; r < requestsPerThread; r++) {
            RateLimitResult result = bucket.tryConsume();
            if (result.allowed()) {
              allowedCount.incrementAndGet();
            } else {
              deniedCount.incrementAndGet();
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }));
    }

    startLatch.countDown(); // Start all threads at once

    // Wait for all threads to complete
    for (Future<?> future : futures) {
      future.get();
    }
    executor.shutdown();

    // Then - exactly 'capacity' requests should be allowed, rest denied
    assertThat(allowedCount.get())
        .as("Exactly %d requests should be allowed (bucket capacity)", capacity)
        .isEqualTo(capacity);

    assertThat(deniedCount.get())
        .as("Remaining %d requests should be denied", totalRequests - capacity)
        .isEqualTo(totalRequests - capacity);

    assertThat(allowedCount.get() + deniedCount.get())
        .as("Total processed requests should equal total sent requests")
        .isEqualTo(totalRequests);
  }

  @Test
  @DisplayName("InMemoryRateLimiter should isolate clients under concurrent load")
  void rateLimiterShouldIsolateClientsUnderConcurrentLoad() throws Exception {
    // Given
    int capacity = 50;
    var config = new RateLimitConfig(capacity, 1); // Minimal refill - won't affect test
    var rateLimiter = new InMemoryRateLimiter(config);

    int clientCount = 5;
    int threadsPerClient = 10;
    int requestsPerThread = 20;

    ExecutorService executor = Executors.newFixedThreadPool(clientCount * threadsPerClient);
    CountDownLatch startLatch = new CountDownLatch(1);

    // Track allowed requests per client
    List<AtomicInteger> allowedPerClient = new ArrayList<>();
    for (int i = 0; i < clientCount; i++) {
      allowedPerClient.add(new AtomicInteger(0));
    }

    // When - multiple clients with multiple threads each
    List<Future<?>> futures = new ArrayList<>();
    for (int c = 0; c < clientCount; c++) {
      final int clientId = c;
      final String clientName = "client-" + c;

      for (int t = 0; t < threadsPerClient; t++) {
        futures.add(executor.submit(() -> {
          try {
            startLatch.await();
            for (int r = 0; r < requestsPerThread; r++) {
              RateLimitResult result = rateLimiter.tryAcquire(clientName);
              if (result.allowed()) {
                allowedPerClient.get(clientId).incrementAndGet();
              }
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }));
      }
    }
    startLatch.countDown();

    for (Future<?> future : futures) {
      future.get();
    }
    executor.shutdown();

    // Then - each client should have exactly 'capacity' allowed requests
    for (int c = 0; c < clientCount; c++) {
      assertThat(allowedPerClient.get(c).get())
          .as("Client-%d should have exactly %d allowed requests", c, capacity)
          .isEqualTo(capacity);
    }
  }
}
