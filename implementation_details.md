# CallBACK - Mapa Architektury i Opis Kodu

Dokument ten opisuje strukturę katalogów oraz odpowiedzialność poszczególnych plików w systemie CallBACK. Architektura opiera się na podziale warstwowym (Clean Architecture / Hexagonal Architecture).

## 1. Warstwa Infrastruktury (`infrastructure`)
Odpowiada za komunikację z bazą danych, zewnętrznymi API oraz przyjmowanie żądań HTTP.

### 1.1. Baza Danych (`persistence.entity` / `persistence.repository`)
*   `ContactEntity.java` – Odwzorowanie tabeli `contacts`. Przechowuje relację `@ManyToOne` do użytkownika, dane kontaktowe oraz przypisaną rangę (Enum `PriorityRole`).
*   `ContactJpaRepository.java` – Interfejs Spring Data JPA. Wymusza podawanie `userId` we wszystkich operacjach (np. `findByUserIdAndPhoneNumber`), chroniąc system przed atakami IDOR.
*   `KeywordEntity.java` – Odwzorowanie tabeli `keywords`. Przechowuje słowo kluczowe znormalizowane do małych liter oraz jego wagę punktową (1-5).
*   `KeywordJpaRepository.java` – Zapewnia operacje CRUD dla słów kluczowych. Zabezpiecza przed dodawaniem duplikatów przez jednego użytkownika (`existsByUserIdAndWordIgnoreCase`).

### 1.2. Interfejsy Webowe REST (`web.controller`)
*   `ContactController.java` – "Kelner" modułu kontaktów. Odbiera żądania HTTP, weryfikuje poprawność JSON-a (`@Valid`) oraz bezpiecznie wyciąga tożsamość użytkownika z tokena JWT (`Principal`), delegując pracę do serwisu.
*   `KeywordController.java` – Wystawia endpointy do zarządzania słowami kluczowymi, rygorystycznie chroniąc przed dostępem bez autoryzacji.

## 2. Warstwa Aplikacji (`application`)
Zawiera główną logikę biznesową, orkiestrację oraz definicje kontraktów wejścia/wyjścia (DTO).

### 2.1. Kontrakty DTO (`application.dto`)
*   `CreateContactRequest` / `UpdateContactRequest` – Struktury wejściowe. Pełnią rolę "bramkarzy" dzięki adnotacjom walidacyjnym (np. `@Pattern` dla numeru telefonu, `@Size` dla imienia). Odrzucają błędne dane zanim dotrą do bazy.
*   `ContactResponse` / `KeywordResponse` – Struktury wyjściowe. Maskują wewnętrzne szczegóły bazy danych i zwracają klientowi mobilnemu tylko bezpieczne, niezbędne informacje.
*   `CreateKeywordRequest` – Waliduje obecność słowa oraz ogranicza zakres wag do przedziału 1-5 (`@Min`, `@Max`).

### 2.2. Serwisy Biznesowe (`application.service`)
*   `ContactServiceImpl.java` – Implementacja logiki zarządzania kontaktami. Wyciąga użytkownika po emailu, weryfikuje duplikaty, mapuje DTO na encję i zapisuje w bazie. Każda metoda (np. aktualizacja, usunięcie) ponownie autoryzuje właściciela zasobu.
*   `KeywordServiceImpl.java` – Odpowiada za operacje na słowach kluczowych. Przed zapisem normalizuje tekst (usuwa spacje, zamienia na małe litery), uodparniając system na błędy użytkownika.

## 3. Silnik Triage i Priorytetyzacji (`application.pipeline` / `application.strategy`)
Serce systemu odpowiadające za wyliczanie wagi przychodzących komunikatów.

*   `MessageProcessingContext.java` – Kontekst przetwarzania. Przechowuje stan początkowy wiadomości, wyniki pośrednie (wykryty kontakt, dopasowane słowa) oraz wynik ostateczny.
*   `PriorityEvaluationStrategy.java` – Główny interfejs (kontrakt) dla wszystkich reguł punktujących.
*   `ContactRoleStrategy.java` – Przydziela punkty bazowe na podstawie tożsamości nadawcy (np. VIP = 50 pkt, STANDARD = 10 pkt).
*   `KeywordMatchStrategy.java` – Analizuje wykryte słowa kluczowe, sumuje ich wagi i mnoży przez 10 (dostosowując do skali 100-punktowej).
*   `PriorityEvaluator.java` – Orkiestrator strategii. Agreguje punkty ze wszystkich dostępnych implementacji reguł i przypisuje ostateczny status priorytetu (CRITICAL, HIGH, MEDIUM, LOW).