package cz.petrklatik.ratelimitter.infrastructure;

import cz.petrklatik.ratelimitter.ratelimiter.InMemoryRateLimiter;
import cz.petrklatik.ratelimitter.ratelimiter.RateLimitConfig;
import cz.petrklatik.ratelimitter.ratelimiter.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


// TODO: Production setup with database:
// - Create ClientRateLimit entity (clientId, capacity, refillPerSecond)
// - Create JPA repository for ClientRateLimit
// - Add CommandLineRunner bean to load configs from DB on startup
@Configuration
public class RateLimitConfiguration {

  @Bean
  public RateLimiter rateLimiter() {
    return new InMemoryRateLimiter(RateLimitConfig.DEFAULT);
  }
}
