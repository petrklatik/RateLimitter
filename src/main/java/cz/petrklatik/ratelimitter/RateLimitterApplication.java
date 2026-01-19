package cz.petrklatik.ratelimitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RateLimitterApplication {

  static void main(String[] args) {
    SpringApplication.run(RateLimitterApplication.class, args);
  }
}
