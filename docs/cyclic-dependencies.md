# Cyklické závislosti

Příklady řešení v [`cz.petrklatik.cyclicdependencies`](../src/main/java/cz/petrklatik/cyclicdependencies/).

## Problém

[`problem/`](../src/main/java/cz/petrklatik/cyclicdependencies/problem/) - ServiceA → ServiceB → ServiceA

## Řešení

1. **Interface Extraction** - [`interfaceextraction/`](../src/main/java/cz/petrklatik/cyclicdependencies/interfaceextraction/) - Dependency Inversion, ServiceA závisí na rozhraní místo konkrétní třídy

2. **Mediator Pattern** - [`mediator/`](../src/main/java/cz/petrklatik/cyclicdependencies/mediator/) - centrální koordinátor, služby se neznají přímo

## Spring řešení

3. **@Lazy** - [`lazy/`](../src/main/java/cz/petrklatik/cyclicdependencies/lazy/) - odložená inicializace, přeruší cyklus při startu

4. **ObjectProvider** - [`objectprovider/`](../src/main/java/cz/petrklatik/cyclicdependencies/objectprovider/) - explicitní lazy lookup přes `ObjectProvider<T>`

---
