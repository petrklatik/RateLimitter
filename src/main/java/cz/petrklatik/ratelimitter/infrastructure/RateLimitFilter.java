package cz.petrklatik.ratelimitter.infrastructure;

import cz.petrklatik.ratelimitter.ratelimiter.RateLimitResult;
import cz.petrklatik.ratelimitter.ratelimiter.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP filter that enforces rate limiting on all requests.
 * Returns 429 Too Many Requests when limit exceeded.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimiter rateLimiter;

  public RateLimitFilter(RateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  private static final String CLIENT_ID_HEADER = "X-Client-Id";

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain chain) throws ServletException, IOException {

    String clientId = request.getHeader(CLIENT_ID_HEADER);
    if (clientId == null || clientId.isBlank()) {
      log.warn("Request without X-Client-Id header: {} {}", request.getMethod(), request.getRequestURI());
      response.setStatus(400);
      response.setContentType("application/problem+json");
      response.getWriter().write("""
          {"type":"about:blank","title":"Bad Request","status":400,"detail":"Missing X-Client-Id header"}
          """);
      return;
    }

    RateLimitResult result = rateLimiter.tryAcquire(clientId);

    response.setIntHeader("X-RateLimit-Limit", result.limit());
    response.setIntHeader("X-RateLimit-Remaining", result.remaining());

    if (result.allowed()) {
      log.info("[{}] ALLOWED - remaining: {}/{}", clientId, result.remaining(), result.limit());
      chain.doFilter(request, response);
    } else {
      log.warn("[{}] DENIED - retry after {}s", clientId, result.retryAfterSeconds());
      response.setStatus(429);
      response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
      response.setContentType("application/problem+json");
      response.getWriter().write("""
          {"type":"about:blank","title":"Too Many Requests","status":429}
          """);
    }
  }
}
