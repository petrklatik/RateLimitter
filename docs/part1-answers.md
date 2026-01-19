# Část 1: Základní design (Single-node)

## 1. Jak byste navrhl(a) rozhraní (interface) pro tuto službu? Jaká by byla signatura hlavní metody?

Navrhl jsem rozhraní [`RateLimiter`](../src/main/java/cz/petrklatik/ratelimitter/ratelimiter/RateLimiter.java) s hlavní metodou `tryAcquire(String clientId)`.

Metoda vrací `RateLimitResult`, ze které se přidávají hlavičky do HTTP response o tom, kolik tokenů zbývá, jaký je limit, a případně za jak dlouho může klient zkusit znovu.

`setClientConfig()` pro runtime změnu limitů bez restartu.

---

## 2. Jaký algoritmus byste použil(a) pro sledování požadavků? Krátce zdůvodněte svou volbu.

Použil jsem **Token Bucket** algoritmus, implementace v [`TokenBucket.java`](../src/main/java/cz/petrklatik/ratelimitter/ratelimiter/TokenBucket.java).

Hlavní důvod: povoluje kontrolované bursty. Klient může poslat najednou víc požadavků (až do `capacity`), ale dlouhodobě je omezen na `refillPerSecond`. To je přesně co chceme pro API - občas potřebujete poslat víc requestů najednou, ale nechcete aby někdo zahltil server.

Zvažoval jsem i **Sliding Window**, ale ten má vyšší paměťovou náročnost a nižší přesnost - musí ukládat timestamp každého požadavku. Token Bucket potřebuje jen 2 hodnoty: počet tokenů a poslední timestamp.

---

## 3. Jakou datovou strukturu byste v Javě použil(a) pro ukládání stavu pro jednotlivé klienty?

Použil jsem `ConcurrentHashMap<String, TokenBucket>` v [`InMemoryRateLimiter.java`](../src/main/java/cz/petrklatik/ratelimitter/ratelimiter/InMemoryRateLimiter.java).

Důvod: metoda `computeIfAbsent()` je atomická - garantuje, že pro každého klienta se vytvoří právě jeden bucket, i když přijde více požadavků současně. Plus má lock striping - různí klienti se neblokují navzájem.

---

## 4. Jak zajistíte, že vaše implementace je thread-safe a zároveň výkonná?

Využil jsem **atomických operací a CAS loopu** místo `synchronized`, viz [`TokenBucket.java:`](../src/main/java/cz/petrklatik/ratelimitter/ratelimiter/TokenBucket.java).

Stav bucketu (`tokens` + `lastRefillNanos`) je v immutable recordu uvnitř `AtomicReference`. Při každém požadavku:
1. Načtu aktuální stav
2. Vypočítám nový stav
3. Zkusím atomicky vyměnit (`compareAndSet`)
4. Pokud někdo jiný mezitím změnil stav, zkusím znovu s čerstvými hodnotami+

Oproti `synchronized` má CAS výhodu v tom, že vlákna se neblokují - když přijde málo požadavků najednou, CAS projde na první pokus. Když jich přijde hodně, prostě se zopakuje výpočet.
