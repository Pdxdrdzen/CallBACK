# CallBACK - System Priorytetyzacji i Triage Powiadomień

CallBACK to system inteligentnej segregacji (triage) wiadomości przychodzących, klasyfikacji priorytetów oraz niezawodnego dostarczania powiadomień Push (FCM) z omijaniem trybu wyciszenia (DND) dla zdarzeń o statusie krytycznym oraz pilnowaniem SLA kontaktu zwrotnego.

---

## 1. Konfiguracja Środowiska Bazodanowego i Kontenerów

### 1.1. Wymagania wstępne
- **Docker** oraz **Docker Compose**
- **Java 17+** (JDK 17 lub JDK 21)
- **PostgreSQL 15** (uruchamiany przez Docker Compose)
- **RabbitMQ 3** z wtyczką Management (uruchamiany przez Docker Compose)

---

### 1.2. Uruchomienie Usług Infrastrukturalnych (Docker Compose)

W głównym katalogu projektu znajduje się plik [docker-compose.yml](file:///c:/Users/Przemyslaw/.gemini/antigravity-ide/scratch/callback-app/docker-compose.yml) konfigurujący kontenery bazodanowe i kolejkowe w izolowanej sieci mostkowej `callback-net`:

```bash
docker compose up -d
```

#### Usługi:
1. **PostgreSQL 15 (`callback-db`)**:
   - Port zewnętrzny: `5432`
   - Baza danych: `callback_db`
   - Użytkownik: `callback_user`
   - Hasło: `callback_password`
   - Healthcheck: `pg_isready -U callback_user -d callback_db -t 3`
   - Limit pamięci RAM: `512MB`
   - Wolumen trwały: `pgdata`

2. **RabbitMQ 3 Management (`callback-rabbitmq`)**:
   - Port protokołu AMQP: `5672`
   - Panel administracyjny HTTP: `http://localhost:15672` (użytkownik: `callback_user`, hasło: `callback_password`)
   - Healthcheck: `rabbitmq-diagnostics -q ping`
   - Limit pamięci RAM: `512MB`
   - Wolumen trwały: `rabbitmq_data`

---

### 1.3. Migracje Bazy Danych (Flyway)

Zarządzanie schematem bazy danych odbywa się za pomocą narzędzia **Flyway**. Zgodnie z zasadą Zero Raw DDL w środowisku produkcyjnym, Hibernate działa wyłącznie w trybie walidacji struktury tabel (`validate`), uniemożliwiając samowolną modyfikację schematu.

- **Katalog migracji**: [`backend/src/main/resources/db/migration/`](file:///c:/Users/Przemyslaw/.gemini/antigravity-ide/scratch/callback-app/backend/src/main/resources/db/migration/)
- **Początkowa migracja**: [`V1__init_schema.sql`](file:///c:/Users/Przemyslaw/.gemini/antigravity-ide/scratch/callback-app/backend/src/main/resources/db/migration/V1__init_schema.sql)

#### Konfiguracja w `application.properties`:
```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/callback_db
spring.datasource.username=callback_user
spring.datasource.password=callback_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate / JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway Migration Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

#### Tabele utworzone w migracji V1:
1. `users` – konta użytkowników, hashe haseł i role systemowe.
2. `contacts` – baza zaufanych kontaktów powiązanych z użytkownikiem wraz z rolami priorytetowymi (`VIP`, `FAMILY`, `MANAGEMENT`, itd.).
3. `emergency_keywords` – słowa kluczowe podnoszące wagę alarmową wraz z obsługą wyrażeń regularnych (ochrona ReDoS).
4. `notification_rules` – konfiguracja reguł powiadomień, profili wibracyjnych, kanałów audio i omijania DND.
5. `incoming_messages` – historia przychodzących komunikatów wraz ze statusem i wyliczonym poziomem priorytetu.
6. `interaction_sla_records` – rekordy SLA pilnujące konieczności kontaktu zwrotnego przed upływem terminu `deadline_at`.
7. `outbox_events` – tabela wzorca Transactional Outbox gwarantująca asynchroniczną, transakcyjną publikację zdarzeń do RabbitMQ.

---

### 1.4. Diagram Relacyjny Bazy Danych (ERD)

Pełny diagram relacji bazodanowych znajduje się w pliku:
- [`uml/database_erd.puml`](file:///c:/Users/Przemyslaw/.gemini/antigravity-ide/scratch/callback-app/uml/database_erd.puml)

Do wygenerowania lub podglądu diagramu w formacie graficznym (PNG/SVG) zalecane jest rozszerzenie PlantUML w IDE lub narzędzie CLI:
```bash
plantuml uml/database_erd.puml
```

---

### 1.5. Uruchomienie Aplikacji Backendowej

Przed uruchomieniem upewnij się, że kontenery Docker pracują:
```bash
# Weryfikacja kontenerów
docker compose ps

# Uruchomienie aplikacji Spring Boot (automatycznie wykona migrację Flyway V1)
cd backend
./gradlew bootRun
```
