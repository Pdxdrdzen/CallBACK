# Dokumentacja API - CallBACK (Faza 1 i 2)

Poniższa dokumentacja opisuje zaimplementowane dotychczas funkcjonalności backendu, wymagane formaty danych (payload) oraz przykładowe odpowiedzi serwera. Wszystkie zabezpieczone endpointy wymagają przekazania nagłówka autoryzacyjnego z tokenem JWT.

## 0. Autoryzacja i autentykacja (Faza 1)
Aby przetestować moduły biznesowe, musisz najpierw uzyskać token dostępu.

**Zalogowanie użytkownika**
* **URL:** `POST /api/v1/auth/login`
* **Wymagane:** Ważne dane logowania w ciele żądania (zależnie od implementacji Fazy 1, zazwyczaj `email` i `password`).
* **Akcja testowa:** Skopiuj wygenerowany token (bez cudzysłowów). W Postmanie w zakładce **Authorization** wybierz typ **Bearer Token** i wklej skopiowany ciąg w pole Token. Ten krok jest wymagany dla wszystkich poniższych żądań.

## 1. Moduł Zarządzania Kontaktami (Faza 2.1)

### Dodawanie nowego kontaktu
* **URL:** `POST /api/v1/contacts`
* **Zasady walidacji:** Imię (max 150 znaków), telefon (wymagany, format z plusem lub bez, cyfry, spacje, myślniki, max 30 znaków), email (opcjonalny, musi być poprawny), rola (VIP, FAMILY, MANAGEMENT, CLIENT, STANDARD).
* **Przykładowy Request (Body -> raw -> JSON):**
```json
{
  "name": "Jan Kowalski",
  "phoneNumber": "+48 123 456 789",
  "email": "jan.kowalski@example.com",
  "priorityRole": "VIP"
}
```
* **Oczekiwany Response (201 Created):**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Jan Kowalski",
  "phoneNumber": "+48 123 456 789",
  "email": "jan.kowalski@example.com",
  "priorityRole": "VIP"
}
```

### Pobieranie listy kontaktów (Paginacja)
* **URL:** `GET /api/v1/contacts?page=0&size=10`
* **Opis:** Pobiera kontakty zalogowanego użytkownika w formie stronicowanej. Parametry `page` (od 0) i `size` (ilość na stronę) są opcjonalne.
* **Oczekiwany Response (200 OK):**
```json
{
  "content": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "name": "Jan Kowalski",
      "phoneNumber": "+48 123 456 789",
      "email": "jan.kowalski@example.com",
      "priorityRole": "VIP"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "unpaged": false,
    "paged": true
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

### Aktualizacja istniejącego kontaktu
* **URL:** `PUT /api/v1/contacts/{id}`
* **Opis:** Podmień `{id}` w URL na UUID z odpowiedzi tworzenia/pobierania kontaktu.
* **Przykładowy Request (Body -> raw -> JSON):**
```json
{
  "name": "Jan Kowalski (Zaktualizowany)",
  "phoneNumber": "+48 111 222 333",
  "email": "jan.nowy@example.com",
  "priorityRole": "FAMILY"
}
```
* **Oczekiwany Response (200 OK):**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Jan Kowalski (Zaktualizowany)",
  "phoneNumber": "+48 111 222 333",
  "email": "jan.nowy@example.com",
  "priorityRole": "FAMILY"
}
```

### Usuwanie kontaktu
* **URL:** `DELETE /api/v1/contacts/{id}`
* **Opis:** Trwale usuwa kontakt z bazy danych dla zalogowanego użytkownika.
* **Oczekiwany Response:** `204 No Content` (puste ciało odpowiedzi, co oznacza pomyślne wykonanie operacji bez zwracania danych).

## 2. Moduł Słów Kluczowych (Faza 2.2)

### Dodawanie słowa kluczowego
* **URL:** `POST /api/v1/keywords`
* **Zasady walidacji:** Słowo kluczowe jest wymagane, nie może być puste (max 50 znaków), system sam zapisuje je jako małe litery i ucina spacje. Waga musi być liczbą całkowitą z przedziału 1-5. Blokowane są duplikaty dla tego samego użytkownika.
* **Przykładowy Request (Body -> raw -> JSON):**
```json
{
  "word": "pilne",
  "weight": 5
}
```
* **Oczekiwany Response (201 Created):**
```json
{
  "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "word": "pilne",
  "weight": 5
}
```

### Pobieranie listy słów kluczowych
* **URL:** `GET /api/v1/keywords`
* **Opis:** Zwraca pełną listę wszystkich słów kluczowych powiązanych z zalogowanym użytkownikiem (w tym przypadku bez paginacji, ponieważ zakłada się relatywnie krótką listę).
* **Oczekiwany Response (200 OK):**
```json
[
  {
    "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "word": "pilne",
    "weight": 5
  },
  {
    "id": "c2a2b3d4-e5f6-47a8-9b1c-2d3e4f5a6b7c",
    "word": "wypadek",
    "weight": 4
  }
]
```

### Usuwanie słowa kluczowego
* **URL:** `DELETE /api/v1/keywords/{id}`
* **Opis:** Podmień `{id}` na UUID pobrane z endpointu GET. System weryfikuje własność rekordu (IDOR protection) przed wykonaniem zrzutu z bazy.
* **Oczekiwany Response:** `204 No Content` (puste ciało odpowiedzi).