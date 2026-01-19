package cz.petrklatik.ratelimitter.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Demo endpoint for testing rate limiting.
 */
@RestController
@RequestMapping("/api")
public class DemoController {

  @GetMapping("/test")
  public Map<String, Object> test() {
    return Map.of(
        "status", "ok",
        "timestamp", Instant.now().toString(),
        "message", "Rate limit headers are in the response"
    );
  }
}
