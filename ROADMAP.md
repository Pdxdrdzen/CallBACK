# CallBACK - Kompleksowa Mapa Drogowa i Specyfikacja Techniczna (Roadmap)

Dokument zawiera szczegółową, inżynierską listę zadań krok po kroku od stanu obecnego do pełnego wdrożenia produkcyjnego. Projekt realizowany jest zgodnie ze standardami architektury czystej (Clean Architecture), Domain-Driven Design (DDD), z pełnym uwzględnieniem wzorców projektowych, zabezpieczeń przed atakami (SQL Injection, XSS, DoS) oraz rygorystycznymi procedurami testowymi.

---

## SPIS TREŚCI
1. [Struktura i Konwencje Projektu](#1-struktura-i-konwencje-projektu)
2. [Faza 0: Środowisko Kontenerowe i Migracje Bazy Danych](#faza-0-środowisko-kontenerowe-i-migracje-bazy-danych)
3. [Faza 1: Backend - Rdzeń Architektury, Bezpieczeństwo i Autoryzacja](#faza-1-backend---rdzeń-architektury-bezpieczeństwo-i-autoryzacja)
4. [Faza 2: Backend - Zarządzanie Kontaktami, Regułami i Słowami Kluczowymi](#faza-2-backend---zarządzanie-kontaktami-regułami-i-słowami-kluczowymi)
5. [Faza 3: Backend - Silnik Triage, Priorytetyzacji i Łańcuch Przetwarzania](#faza-3-backend---silnik-triage-priorytetyzacji-i-łańcuch-przetwarzania)
6. [Faza 4: Backend - Kolejkowanie (RabbitMQ), Outbox Pattern i Integracja FCM](#faza-4-backend---kolejkowanie-rabbitmq-outbox-pattern-i-integracja-fcm)
7. [Faza 5: Frontend Mobilny - Fundamenty i Autoryzacja (React Native / Expo 57)](#faza-5-frontend-mobilny---fundamenty-i-autoryzacja-react-native--expo-57)
8. [Faza 6: Frontend Mobilny - Ekrany Biznesowe i Doświadczenie Użytkownika](#faza-6-frontend-mobilny---ekrany-biznesowe-i-doświadczenie-użytkownika)
9. [Faza 7: Frontend Mobilny - Odbiór Powiadomień Push i Tło](#faza-7-frontend-mobilny---odbiór-powiadomień-push-i-tło)
10. [Faza 8: Testy Automatyczne, Audyt Bezpieczeństwa, Diagramy i Dokumentacja](#faza-8-testy-automatyczne-audyt-bezpieczeństwa-diagramy-i-dokumentacja)

---

## 1. STRUKTURA I KONWENCJE PROJEKTU

### 1.1. Standardy Bezpieczeństwa (Security-First)
- **Zero Raw Queries**: Wszelkie operacje na bazie PostgreSQL przechodzą przez parametryzowane zapytania JPA / JPQL lub bezpieczne metody interfejsów repozytoriów. Żaden fragment SQL nie może być sklejany operatorem `+` ani konkatenacją ciągów.
- **Sanityzacja Danych Wejściowych**: Wszystkie dane tekstowe z zewnątrz podlegają oczyszczaniu z tagów HTML, JavaScript oraz sekwencji sterujących przed przekazaniem do silnika reguł.
- **Ochrona przed DoS i Brute-Force**: Zastosowanie algorytmu Token Bucket (`Bucket4j`) na endpointach logowania, rejestracji oraz ingestion webhooków.
- **Zabezpieczenie przed ReDoS**: Wyrażenia regularne definiowane przez użytkownika przechodzą walidację złożoności i są wykonywane z ograniczonym czasem CPU (timeout).
- **Zasada Najmniejszych Uprawnień**: Izolacja tożsamości użytkowników – każde zapytanie modyfikujące lub odczytujące dane weryfikuje przynależność zasobu do `userId` zalogowanego podmiotu (ochrona przed IDOR).

### 1.2. Konwencje Wizualne i UI
- **Zero infantylnych emotikonów**: Zamiast systemowych emoji stosowane są wyłącznie profesjonalne ikony wektorowe (Lucide Icons / SVG).
- **Spójna paleta kolorów**: Kodowanie priorytetów (Critical: Karmazyn/Czerwień alarmowa `#E11D48`, High: Głęboki Bursztyn `#D97706`, Medium: Indygo/Niebieski `#2563EB`, Low: Szarość techniczna `#64748B`).

---

## FAZA 0: ŚRODOWISKO KONTENEROWE I MIGRACJE BAZY DANYCH

### Krok 0.1: Wzmocnienie konfiguracji Docker Compose
- **Plik**: `docker-compose.yml`
- **Podzadanie 0.1.1**: Dodanie dedykowanej sieci mostkowej `callback-net` izolującej kontenery przed bezpośrednim dostępem z zewnątrz (w środowisku produkcyjnym porty bazodanowe nie są wystawiane publicznie).
- **Podzadanie 0.1.2**: Dodanie sekcji `healthcheck` dla PostgreSQL (`pg_isready -U callback_user -d callback_db -t 3`) oraz RabbitMQ (`rabbitmq-diagnostics -q ping`).
- **Podzadanie 0.1.3**: Skonfigurowanie limitów pamięciowych (`deploy.resources.limits.memory`) dla stabilności działania.

### Krok 0.2: Migracje Bazy Danych (Flyway)
- **Konfiguracja**: Dodanie w `backend/build.gradle` zależności `org.flywaydb:flyway-core` oraz `org.flywaydb:flyway-database-postgresql`.
- **Wyłączenie**: Zmiana w `application.properties` z `spring.jpa.hibernate.ddl-auto=update` na `spring.jpa.hibernate.ddl-auto=validate` oraz aktywacja Flyway.
- **Plik**: `backend/src/main/resources/db/migration/V1__init_schema.sql`
  - **Tabela `users`**:
    - Kolumny: `id UUID PRIMARY KEY`, `email VARCHAR(255) NOT NULL UNIQUE`, `password_hash VARCHAR(255) NOT NULL`, `role VARCHAR(50) NOT NULL`, `created_at TIMESTAMPTZ NOT NULL`, `updated_at TIMESTAMPTZ NOT NULL`.
  - **Tabela `contacts`**:
    - Kolumny: `id UUID PRIMARY KEY`, `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`, `name VARCHAR(150) NOT NULL`, `phone_number VARCHAR(30) NOT NULL`, `email VARCHAR(255)`, `priority_role VARCHAR(50) NOT NULL`, `created_at TIMESTAMPTZ NOT NULL`.
    - Indeksy: `idx_contacts_user_phone (user_id, phone_number)`.
  - **Tabela `emergency_keywords`**:
    - Kolumny: `id UUID PRIMARY KEY`, `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`, `keyword VARCHAR(100) NOT NULL`, `weight INT NOT NULL CHECK (weight BETWEEN 1 AND 100)`, `is_regex BOOLEAN NOT NULL DEFAULT FALSE`, `is_active BOOLEAN NOT NULL DEFAULT TRUE`.
    - Indeksy: `idx_keywords_user_active (user_id, is_active)`.
  - **Tabela `notification_rules`**:
    - Kolumny: `id UUID PRIMARY KEY`, `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`, `min_priority VARCHAR(50) NOT NULL`, `sound_channel VARCHAR(100) NOT NULL`, `vibration_pattern VARCHAR(100) NOT NULL`, `override_dnd BOOLEAN NOT NULL DEFAULT FALSE`.
  - **Tabela `incoming_messages`**:
    - Kolumny: `id UUID PRIMARY KEY`, `recipient_user_id UUID NOT NULL REFERENCES users(id)`, `sender_identifier VARCHAR(100) NOT NULL`, `raw_content TEXT NOT NULL`, `sanitized_content TEXT NOT NULL`, `calculated_priority VARCHAR(50) NOT NULL`, `status VARCHAR(50) NOT NULL`, `created_at TIMESTAMPTZ NOT NULL`.
  - **Tabela `interaction_sla_records`**:
    - Kolumny: `id UUID PRIMARY KEY`, `message_id UUID NOT NULL REFERENCES incoming_messages(id) ON DELETE CASCADE`, `user_id UUID NOT NULL REFERENCES users(id)`, `deadline_at TIMESTAMPTZ NOT NULL`, `status VARCHAR(50) NOT NULL`, `acknowledged_at TIMESTAMPTZ`.
  - **Tabela `outbox_events`**:
    - Kolumny: `id UUID PRIMARY KEY`, `aggregate_type VARCHAR(100) NOT NULL`, `aggregate_id VARCHAR(100) NOT NULL`, `event_type VARCHAR(100) NOT NULL`, `payload JSONB NOT NULL`, `created_at TIMESTAMPTZ NOT NULL`, `processed BOOLEAN NOT NULL DEFAULT FALSE`.
    - Indeksy: `idx_outbox_unprocessed (processed, created_at) WHERE processed = FALSE`.

### Krok 0.3: Dokumentacja Bazy Danych
- **Plik diagramu**: `uml/database_erd.puml` – pełen schemat relacyjny tabel z kluczami obcymi i indeksami.
- **Aktualizacja README**: Wpisanie sekcji konfiguracji środowiska bazodanowego.

---

## FAZA 1: BACKEND - RDZEŃ ARCHITEKTURY, BEZPIECZEŃSTWO I AUTORYZACJA

### Krok 1.1: Konfiguracja Zależności Gradle
- **Plik**: `backend/build.gradle`
- Dodanie bibliotek produkcyjnych:
  - `spring-boot-starter-security` (bezpieczeństwo i filtry)
  - `spring-boot-starter-validation` (walidacja DTO)
  - `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (obsługa JWT)
  - `com.bucket4j:bucket4j-core` (ochrona Rate Limiting)
  - `org.projectlombok:lombok` (wzorce Builder, Gettery, Konstruktory)
  - `org.springdoc:springdoc-openapi-starter-webmvc-ui` (Swagger UI / OpenAPI)
  - `com.google.firebase:firebase-admin` (klient Push)
  - `org.testcontainers:postgresql`, `org.testcontainers:rabbitmq` (testy integracyjne)

### Krok 1.2: Architektura Pakietów (Hexagonal / Clean Architecture)
- Utworzenie struktury pakietów:
  - `com.callback.backend.common`
  - `com.callback.backend.config`
  - `com.callback.backend.domain.model`
  - `com.callback.backend.domain.repository`
  - `com.callback.backend.application.dto`
  - `com.callback.backend.application.service`
  - `com.callback.backend.application.pipeline`
  - `com.callback.backend.application.strategy`
  - `com.callback.backend.infrastructure.persistence`
  - `com.callback.backend.infrastructure.messaging`
  - `com.callback.backend.infrastructure.fcm`
  - `com.callback.backend.infrastructure.web`

### Krok 1.3: Warstwa Bezpieczeństwa (Security Layer)
- **Wzorce projektowe**: Filter Chain, Strategy, Builder.
- **Klasa**: `com.callback.backend.config.SecurityConfig`
  - Metoda `SecurityFilterChain filterChain(HttpSecurity http)`:
    - Bezstanowa sesja (`SessionCreationPolicy.STATELESS`).
    - Wyłączenie CSRF dla API REST.
    - Konfiguracja nagłówków bezpieczeństwa (`HSTS`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`).
    - Autoryzacja endpointów: `/api/v1/auth/**` publiczne, pozostałe wymagają ważnego Bearer Token.
- **Klasa**: `com.callback.backend.infrastructure.web.filter.JwtAuthenticationFilter`
  - Metoda: `void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)`
  - Wyciąganie tokena z nagłówka `Authorization: Bearer <token>`, walidacja podpisu kryptograficznego HMAC-SHA256, wstrzyknięcie tożsamości do `SecurityContextHolder`.
- **Klasa**: `com.callback.backend.infrastructure.web.filter.RateLimitingFilter`
  - Wykorzystanie `Bucket4j`.
  - Ograniczenie żądań na minutę w oparciu o adres IP (dla logowania) lub API Key (dla ingestion). Zwraca kod `429 Too Many Requests`.
- **Klasa**: `com.callback.backend.common.security.InputSanitizer`
  - Metoda: `String sanitize(String rawInput)`
  - Usuwanie niebezpiecznych znaków sterujących i znaczników kodu HTML/JavaScript. Zabezpiecza przed wstrzykiwaniem skryptów i zniekształcaniem zapytań.

### Krok 1.4: Moduł Autoryzacji (Auth Module)
- **Interfejs**: `com.callback.backend.application.service.AuthService`
  - `AuthResponse register(RegisterRequest request)`: Hashowanie hasła algorytmem BCrypt (siła 12), utworzenie rekordu użytkownika w transakcji, wygenerowanie pary tokenów (Access + Refresh).
  - `AuthResponse login(LoginRequest request)`: Walidacja danych uwierzytelniających, ochrona przed timing attacks, generowanie tokenów.
  - `AuthResponse refreshToken(RefreshTokenRequest request)`: Weryfikacja Refresh Tokena z rotacją (stary zostaje unieważniony).
- **Klasa**: `com.callback.backend.infrastructure.web.controller.AuthController`
  - Endpointy: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`.
  - Ścisła walidacja DTO (`@Valid`, `@Email`, `@Size(min = 8, max = 100)`).
- **Testy**:
  - `AuthServiceTest.java`: Testy jednostkowe walidacji haseł i generowania tokenów.
  - `SecurityIntegrationTest.java`: Test próby dostępu do chronionych zasobów bez tokena oraz z nieprawidłowym podpisem.

---

## FAZA 2: BACKEND - ZARZĄDZANIE KONTAKTAMI, REGUŁAMI I SŁOWAMI KLUCZOWYMI

### Krok 2.1: Moduł Kontaktów (Contact Management)
- **Wzorce**: Repository Pattern, Builder Pattern, DTO Pattern.
- **Klasa encji**: `com.callback.backend.infrastructure.persistence.entity.ContactEntity`
  - Pola: `id (UUID)`, `user (UserEntity)`, `name (String)`, `phoneNumber (String)`, `email (String)`, `priorityRole (PriorityRole ENUM: VIP, FAMILY, MANAGEMENT, CLIENT, STANDARD)`.
- **Repozytorium**: `com.callback.backend.infrastructure.persistence.repository.ContactJpaRepository`
  - `Optional<ContactEntity> findByUserIdAndPhoneNumber(UUID userId, String phoneNumber)`
  - `Page<ContactEntity> findAllByUserId(UUID userId, Pageable pageable)`
  - `boolean existsByUserIdAndPhoneNumber(UUID userId, String phoneNumber)`
- **Klasa serwisu**: `com.callback.backend.application.service.ContactService`
  - `ContactResponse createContact(UUID userId, CreateContactRequest req)`
  - `ContactResponse updateContact(UUID userId, UUID contactId, UpdateContactRequest req)`
  - `void deleteContact(UUID userId, UUID contactId)`
  - `Page<ContactResponse> getContacts(UUID userId, Pageable pageable)`
- **Kontroler**: `com.callback.backend.infrastructure.web.controller.ContactController`
  - Bezpieczne pobieranie `userId` wyłącznie z kontekstu zalogowanego użytkownika (`@AuthenticationPrincipal`).

### Krok 2.2: Moduł Słów Kluczowych Alarmowych (Emergency Keywords)
- **Wzorce**: Specification Pattern, Strategy Pattern.
- **Klasa encji**: `EmergencyKeywordEntity`
  - Pola: `id`, `user`, `keyword`, `weight (1..100)`, `isRegex (boolean)`, `isActive (boolean)`.
- **Klasa serwisu**: `com.callback.backend.application.service.EmergencyKeywordService`
  - `KeywordResponse addKeyword(UUID userId, CreateKeywordRequest req)`: Walidacja bezpieczeństwa wyrażeń regularnych (ochrona przed ReDoS).
  - `List<KeywordResponse> getUserKeywords(UUID userId)`
  - `void toggleKeyword(UUID userId, UUID keywordId, boolean active)`
  - `void deleteKeyword(UUID userId, UUID keywordId)`

### Krok 2.3: Moduł Reguł Powiadomień (Notification Rules)
- **Klasa serwisu**: `com.callback.backend.application.service.NotificationRuleService`
  - Konfiguracja przypisań: priorytet komunikatu -> kanał dźwiękowy, wzorzec wibracji, flaga `overrideDnd` (omijanie wyciszenia telefonu).

---

## FAZA 3: BACKEND - SILNIK TRIAGE, PRIORYTETYZACJI I ŁAŃCUCH PRZETWARZANIA

### Krok 3.1: Łańcuch Odpowiedzialności (Chain of Responsibility) dla Ingestion
- **Wzorzec projektowy**: Chain of Responsibility.
- **Klasa kontekstu**: `com.callback.backend.application.pipeline.MessageProcessingContext`
  - Przechowuje stan w trakcie przetwarzania: `rawPayload`, `sanitizedContent`, `senderIdentifier`, `recipientUserId`, `matchedContact`, `matchedKeywords`, `priorityScore`, `finalPriorityLevel`.
- **Interfejs**: `com.callback.backend.application.pipeline.IngestionPipelineStep`
  - `void process(MessageProcessingContext context)`
  - `int getOrder()`
- **Kroki potoku**:
  1. `10_SanitizationStep`: Oczyszczenie tekstu przez `InputSanitizer`.
  2. `20_ContactLookupStep`: Wyszukanie nadawcy w bazie kontaktów użytkownika; przypisanie roli.
  3. `30_KeywordAnalysisStep`: Analiza treści pod kątem aktywnych słów kluczowych; sumowanie wag.
  4. `40_PriorityCalculationStep`: Zastosowanie silnika strategii priorytetyzacji.
  5. `50_OutboxPersistenceStep`: Zapis komunikatu do `incoming_messages` oraz zdarzenia do `outbox_events` w jednej transakcji ACID (`@Transactional`).
- **Orkiestrator**: `com.callback.backend.application.pipeline.MessageIngestionPipeline`
  - Metoda: `IngestionResult execute(IncomingMessageCommand command)`
  - Uruchamia sekwencyjnie zarejestrowane kroki w kontrolowanym bloku `try-catch` z audytem błędów.

### Krok 3.2: Silnik Oceny Priorytetów (Strategy Pattern)
- **Wzorzec projektowy**: Strategy Pattern.
- **Interfejs**: `com.callback.backend.application.strategy.PriorityEvaluationStrategy`
  - `int calculateScore(MessageProcessingContext context)`
- **Implementacje**:
  - `KeywordMatchStrategy`: Sprawdza trafienia słów kluczowych; dodaje punkty wg zdefiniowanych wag.
  - `ContactRoleStrategy`: Przypisuje punkty bazowe: `VIP` (50 pkt), `MANAGEMENT` (40 pkt), `FAMILY` (45 pkt), `CLIENT` (25 pkt), `STANDARD` (10 pkt), `UNKNOWN` (5 pkt).
  - `TimeWindowStrategy`: Modyfikuje punkty w oparciu o godzinę (np. godziny nocne lub dyżury).
- **Klasa wyliczająca**: `com.callback.backend.application.strategy.PriorityEvaluator`
  - Oblicza łączną punktację i mapuje na typ wyliczeniowy `PriorityLevel`:
    - `CRITICAL` (>= 80 punktów)
    - `HIGH` (50 - 79 punktów)
    - `MEDIUM` (25 - 49 punktów)
    - `LOW` (< 25 punktów)

---

## FAZA 4: BACKEND - KOLEJKOWANIE (RABBITMQ), OUTBOX PATTERN I INTEGRACJA FCM

### Krok 4.1: Konfiguracja RabbitMQ i Niezawodne Kolejkowanie
- **Wzorce**: Transactional Outbox Pattern, Dead Letter Exchange (DLX).
- **Klasa**: `com.callback.backend.config.RabbitMQConfig`
  - **Exchange główny**: `callback.direct.exchange` (Direct).
  - **Exchange awaryjny**: `callback.dlx.exchange` (Fanout / Direct DLX).
  - **Kolejki**:
    - `callback.queue.critical` (routing: `priority.critical`, dead-letter przekierowuje do DLX).
    - `callback.queue.high` (routing: `priority.high`).
    - `callback.queue.standard` (routing: `priority.standard`).
    - `callback.queue.dlq` (kolejka odrzuconych wiadomości po wyczerpaniu limitu ponowień).
  - **Serializacja**: `Jackson2JsonMessageConverter` dla bezpiecznego formatu JSON.
- **Klasa**: `com.callback.backend.infrastructure.messaging.OutboxEventPublisher`
  - Cykliczny harmonogram (`@Scheduled(fixedDelay = 500)`).
  - Pobieranie nieprzetworzonych zdarzeń z blokadą `FOR UPDATE SKIP LOCKED`.
  - Publikacja wiadomości do brokera AMQP z potwierdzeniem (Publisher Confirms).
  - Oznaczenie jako `processed = true`.

### Krok 4.2: Konsument Zdarzeń i Klient FCM (Firebase Cloud Messaging)
- **Wzorce**: Factory Pattern (tworzenie dedykowanych payloadów na platformy Android / iOS), Adapter Pattern.
- **Klasa**: `com.callback.backend.infrastructure.fcm.FcmMessageFactory`
  - Metoda: `Message createFcmMessage(NotificationTask task)`
  - Konfiguracja dla kanału krytycznego (Android: `high_priority`, custom sound, bypass DND; iOS: APNs critical alert payload).
- **Klasa**: `com.callback.backend.infrastructure.messaging.NotificationConsumer`
  - Obsługa wiadomości z kolejek:
    - Odbiór zadania wysyłki.
    - Wywołanie `FcmPushService`.
    - Ręczne potwierdzenie `channel.basicAck()` po udanej wysyłce.
    - W razie błędu sieciowego: ponowienie z mechanizmem retry i backoff; po 3 nieudanych próbach przekierowanie do `DLQ`.

### Krok 4.3: Monitor Czasu Interakcji i SLA (Callback SLA Monitor)
- **Zadanie**: Pilnowanie czasu reakcji na komunikaty krytyczne.
- **Klasa**: `com.callback.backend.application.service.SlaMonitoringService`
  - Rejestracja rekordu w tabeli `interaction_sla_records` dla każdego komunikatu `CRITICAL` lub `HIGH` z terminem (np. czas + 15 min).
  - Endpoint potwierdzenia: `POST /api/v1/interactions/{messageId}/acknowledge` – wywoływany przez aplikację mobilną po kontakcie.
  - Cykliczne sprawdzanie: `@Scheduled(fixedRate = 30000) void checkOverdueRecords()`. W przypadku przekroczenia terminu publikowane jest zdarzenie eskalacji `CallbackOverdueAlert` na kolejkę krytyczną.

---

## FAZA 5: FRONTEND MOBILNY - FUNDAMENTY I AUTORYZACJA (REACT NATIVE / EXPO 57)

### Krok 5.1: Przygotowanie Projektu i Zależności
- **Katalog**: `mobile/`
- Instalacja wymaganych pakietów:
  - `expo-notifications` (obsługa Push i powiadomień lokalnych)
  - `expo-secure-store` (bezpieczne przechowywanie tokenów JWT w Keystore/Keychain)
  - `expo-haptics` (haptyka wibracji)
  - `@react-navigation/native`, `@react-navigation/native-stack`, `@react-navigation/bottom-tabs`
  - `zustand` (zarządzanie stanem)
  - `axios` (klient API)
  - `lucide-react-native` (wektorowe ikony interfejsu)

### Krok 5.2: Bezpieczny Klient HTTP i Zarządzanie Sesją
- **Plik**: `mobile/src/api/client.ts`
  - Automatyczne dołączanie nagłówka `Authorization: Bearer <accessToken>`.
  - Interceptor odpowiedzi: przechwytywanie błędu 401 Unauthorized, wykonanie cichego odświeżenia tokena przez endpoint `/auth/refresh` i ponowienie zapytania.
  - W przypadku trwałego błędu: bezpieczne wyczyszczenie pamięci `SecureStore` i przekierowanie do logowania.
- **Plik**: `mobile/src/store/useAuthStore.ts`
  - Stan: `user`, `isAuthenticated`, `isLoading`.
  - Akcje: `login(email, password)`, `register(email, password)`, `logout()`.

---

## FAZA 6: FRONTEND MOBILNY - EKRANY BIZNESOWE I DOŚWIADCZENIE UŻYTKOWNIKA

### Krok 6.1: Dashboard Aktywnych Alertów i Monitora SLA
- **Ekran**: `ActiveAlertsScreen.tsx`
  - Lista bieżących zgłoszeń wymagających oddzwonienia.
  - Komponent `SlaTimer.tsx`: licznik odliczający czas do upływu SLA z kolorystyką zależną od pozostałego czasu (zielony -> żółty -> pulsujący karmazyn).
  - Przyciski szybkiej reakcji:
    - *Oddzwoń*: automatyczne otwarcie dialera telefonu z numerem nadawcy.
    - *Potwierdź kontakt*: wywołanie `/acknowledge` i zatrzymanie licznika.
    - *Odłóż*: prolongata o określony interwał (np. 15 min).

### Krok 6.2: Ekran Zarządzania Kontaktami i Priorytetami
- **Ekran**: `ContactsScreen.tsx`
  - Wyszukiwarka kontaktów, grupowanie wg ról (`VIP`, `FAMILY`, `MANAGEMENT`, itd.).
  - Modal edycji kontaktu: przypisanie roli, dedykowanego priorytetu i notatek.

### Krok 6.3: Ekran Konfiguracji Słów Kluczowych Alarmowych
- **Ekran**: `KeywordsConfigScreen.tsx`
  - Lista słów kluczowych z wagami punktowymi.
  - Możliwość testowania wykrywania słów w symulatorze wiadomości bezpośrednio na urządzeniu.

### Krok 6.4: Ekran Ustawień Powiadomień i Obejścia Trybu Wyciszenia
- **Ekran**: `NotificationSettingsScreen.tsx`
  - Przypisywanie profili dźwiękowych i haptycznych do poziomów priorytetu.
  - Konfiguracja uprawnień "Critical Alerts" (iOS) oraz kanałów powiadomień o wysokim priorytecie (Android).

---

## FAZA 7: FRONTEND MOBILNY - ODBIÓR POWIADOMIEŃ PUSH I TŁO

### Krok 7.1: Rejestracja Urządzenia w FCM
- **Hook**: `mobile/src/hooks/usePushNotifications.ts`
  - Prośba o uprawnienia do powiadomień.
  - Pobranie unikalnego tokena FCM (`Notifications.getDevicePushTokenAsync()`).
  - Przesłanie tokena do backendu (`POST /api/v1/users/device-token`) i powiązanie z kontem.

### Krok 7.2: Reakcja na Przychodzące Powiadomienia
- Konfiguracja listenerów:
  - `addNotificationReceivedListener`: Odbiór w trakcie działania aplikacji (wyświetlenie banera in-app i uruchomienie wibracji).
  - `addNotificationResponseReceivedListener`: Reakcja na kliknięcie w powiadomienie (przejście bezpośrednio do detali danego alertu w dashboardzie).

---

## FAZA 8: TESTY AUTOMATYCZNE, AUDYT BEZPIECZEŃSTWA, DIAGRAMY I DOKUMENTACJA

### Krok 8.1: Zestaw Testów Automatycznych
- **Testy jednostkowe (JUnit 5 + Mockito)**:
  - `InputSanitizerTest`: weryfikacja usuwania tagów `<script>`, cudzysłowów i złośliwych sekwencji.
  - `PriorityEvaluationStrategyTest`: testy poprawności kalkulacji punktowej we wszystkich gałęziach logicznych.
  - `MessageIngestionPipelineTest`: izolowane testy każdego kroku potoku przetwarzania.
- **Testy integracyjne (Testcontainers)**:
  - `IngestionFlowIntegrationTest`: uruchomienie kontenerów PostgreSQL i RabbitMQ, weryfikacja przejścia wiadomości od kontrolera przez bazę danych aż do brokera.
  - `RateLimitingIntegrationTest`: weryfikacja zablokowania kolejnych żądań po przekroczeniu limitu.
- **Testy komponentów mobilnych (Jest + Testing Library)**:
  - Test zachowania komponentu `SlaTimer` przy upływie czasu.
  - Test poprawnego formatowania numerów telefonów w `AddEditContactScreen`.

### Krok 8.2: Diagramy Systemowe
- **Pliki diagramów**:
  - `uml/c4_architecture.puml`: Architektura systemu na poziomie kontenerów C4.
  - `uml/sequence_ingestion.puml`: Przebieg komunikacji od odebrania wiadomości do wystawienia notyfikacji FCM.
  - `uml/sequence_sla_monitoring.puml`: Przebieg monitorowania SLA i procedury eskalacji powiadomienia.

### Krok 8.3: Audyt Bezpieczeństwa (OWASP Compliance)
- Weryfikacja odporności na SQL Injection (analiza kodu pod kątem dynamicznego SQL).
- Weryfikacja bezpieczeństwa nagłówków HTTP i konfiguracji CORS.
- Sprawdzenie odporności na ataki typu ReDoS w module słów kluczowych.

### Krok 8.4: Aktualizacja Dokumentacji (`README.md`)
- Aktualizacja głównego pliku `README.md` o:
  - Pełen opis funkcjonalny systemu.
  - Instrukcję uruchomienia środowiska deweloperskiego (Docker, Gradle, Expo).
  - Dokumentację zmiennych środowiskowych.
  - Adresy interfejsów diagnostycznych (Swagger UI: `http://localhost:8080/swagger-ui.html`, RabbitMQ Management: `http://localhost:15672`).
