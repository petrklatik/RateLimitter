# Část 2: Distribuovaný systém (Multi-node)

## 1. Jaký problém nastane s implementací z Části 1, pokud ji nasadíme na více serverů za load balancer?

Problém je, že každá instance má vlastní in-memory mapu s buckety. Load balancer rozhodí requesty mezi instance, takže klient reálně dostane násobek limitu podle počtu běžících nodů.

---

## 2. Jak byste upravil(a) svůj návrh, aby Rate Limiting fungoval konzistentně napříč všemi instancemi?

Použil bych **Redis** jako sdílené úložiště pro stav bucketů - všechny instance čtou a zapisují do stejného místa.

Token Bucket logika by běžela jako Lua script přímo v Redisu - to zajistí atomicitu (načtení stavu, výpočet, zápis proběhne jako jedna operace). Redis má nízkou latenci a podporuje TTL pro automatickou expiraci neaktivních klientů.

Popis implementace je v [`ratelimiter/distributed/`](../src/main/java/cz/petrklatik/ratelimitter/ratelimiter/distributed/).
