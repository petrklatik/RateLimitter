# Část 3: Konfigurace a testování

## 1. Jak byste navrhl(a) systém, aby bylo možné snadno měnit limity pro různé klienty bez nutnosti restartu služby?

Současná implementace toto podporuje. Metoda [`setClientConfig()`](../src/main/java/cz/petrklatik/ratelimitter/ratelimiter/InMemoryRateLimiter.java) umožňuje změnit
limit za běhu:

```java
rateLimiter.setClientConfig("client",new RateLimitConfig(1000, 100));
```

Při změně konfigurace se zachovává aktuální poměr naplnění bucketu - když měl klient využitých 50% tokenů, po změně
limitu bude mít stále 50%.

V produkcnim prostředí by pro změny configu existoval endpoint.

2. **Perzistence v databázi** - konfigurace se načte při startu a cachuje s TTL. Změna přes API zapíše do DB a
   invaliduje cache.
3. **Sdílená Redis cache** - konfigurace klientů se cachuje v Redisu (`@Cacheable`). Při změně konfigurace se cache
   invaliduje (`@CacheEvict`), další request pak načte aktuální data z DB (lazy reload).

---

## 2. Jak byste testoval(a) tuto funkcionalitu, zejména v prostředí s vysokou zátěží a vícevláknovým zpracováním?

Testovací strategie pro rate limiter pokrývá několik úrovní:

### Unit testy

Ověření samostatně správnost jednotlivých komponent - algoritmus token bucket (konzumace tokenů, refill logika),
per-client izolaci a správné chování při změně konfigurace za běhu, atd.

### Concurrency testy

Kritické pro rate limiter, který musí být thread-safe. Testy spouští desítky vláken.

### Integrační testy

Testují celý flow přes HTTP vrstvu pomocí `@SpringBootTest` a `MockMvc`. Ověřují správné HTTP status kódy (200, 429,
400), response headers a integraci rate limiteru se Spring filtrem.

### Performance a load testing

Pro testování pod realistickou zátěží bych využil nástroje jako **k6** nebo **JMeter**, které jsou k tomu určeny a mají
v sobě i monitoring.