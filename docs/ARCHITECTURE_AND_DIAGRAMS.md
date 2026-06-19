# System Architecture & Sequence Diagrams

## 1. System Architecture Diagram

```mermaid
flowchart TB
    subgraph Exotel["Exotel Cloud Telephony"]
        EX[Exotel Call Flow / Exoline Number]
    end

    subgraph App["Spring Boot Application (Layered Architecture)"]
        direction TB
        WC[Controller Layer<br/>ExotelWebhookController<br/>MissedCallController<br/>DashboardController]
        SV[Service Layer<br/>MissedCallService<br/>CallerService]
        RP[Repository Layer<br/>MissedCallRepository<br/>CallerRepository]
        EN[Entity Layer<br/>MissedCall, Caller]
        EX_H[GlobalExceptionHandler]
        SW[Swagger / OpenAPI UI]
        TH[Thymeleaf Dashboard]
    end

    subgraph DB["MySQL 8.0"]
        T1[(callers)]
        T2[(missed_calls)]
    end

    Browser[Admin Browser]

    EX -->|"POST /api/exotel/webhook (missed call event)"| WC
    WC --> SV
    SV --> RP
    RP --> EN
    EN -->|JPA/Hibernate| DB
    WC -.->|errors| EX_H

    Browser -->|"GET /dashboard"| TH
    TH --> SV
    Browser -->|"REST calls /api/missed-calls/**"| WC
    Browser -->|"/swagger-ui.html"| SW
```

**Layer responsibilities:**
- **Controller Layer** — HTTP entry points; request parsing, response shaping, no business logic.
- **Service Layer** — Business rules: idempotency (duplicate CallSid check), caller-name resolution, status filtering, date range logic.
- **Repository Layer** — Spring Data JPA interfaces; query derivation and custom JPQL.
- **Entity Layer** — JPA-mapped domain objects (`MissedCall`, `Caller`) persisted to MySQL.
- **Exception Layer** — Centralized `@RestControllerAdvice` translating exceptions to consistent JSON error responses.

---

## 2. Sequence Diagram — Missed Call Webhook Processing

```mermaid
sequenceDiagram
    participant Exotel as Exotel Cloud
    participant Ctrl as ExotelWebhookController
    participant Svc as MissedCallService
    participant CallerSvc as CallerService
    participant Repo as MissedCallRepository
    participant DB as MySQL

    Exotel->>Ctrl: POST /api/exotel/webhook (CallSid, From, Status, StartTime)
    Ctrl->>Ctrl: parseRequest() - case-insensitive field extraction
    Ctrl->>Ctrl: validate CallSid & From present

    alt Status is NOT a missed-call status (e.g. completed)
        Ctrl-->>Exotel: 200 OK "not processed"
    else Status IS a missed-call status
        Ctrl->>Svc: processWebhookEvent(request)
        Svc->>Repo: existsByCallSid(callSid)
        Repo->>DB: SELECT ... WHERE call_sid = ?
        DB-->>Repo: exists / not exists

        alt CallSid already exists (duplicate webhook)
            Svc-->>Ctrl: throw DuplicateCallSidException
            Ctrl-->>Exotel: 200 OK "already processed"
        else New CallSid
            Svc->>CallerSvc: resolveCallerName(callerNumber)
            CallerSvc->>DB: SELECT name FROM callers WHERE phone_number = ?
            DB-->>CallerSvc: name or empty
            CallerSvc-->>Svc: name ("Unknown" if not found)
            Svc->>Svc: parse missedCallTime (StartTime/EndTime)
            Svc->>Repo: save(MissedCall)
            Repo->>DB: INSERT INTO missed_calls (...)
            DB-->>Repo: saved row (id)
            Repo-->>Svc: MissedCall entity
            Svc-->>Ctrl: MissedCallResponse
            Ctrl-->>Exotel: 201 Created + JSON body
        end
    end
```

---

## 3. Sequence Diagram — Dashboard View Request

```mermaid
sequenceDiagram
    participant Admin as Admin Browser
    participant DashCtrl as DashboardController
    participant Svc as MissedCallService
    participant Repo as MissedCallRepository
    participant DB as MySQL
    participant View as Thymeleaf Template

    Admin->>DashCtrl: GET /dashboard?phoneNumber=&fromDate=&toDate=&page=0
    DashCtrl->>Svc: search(phoneNumber, fromDate, toDate, pageable)
    Svc->>Repo: search(...) JPQL query
    Repo->>DB: SELECT ... WHERE filters
    DB-->>Repo: page of MissedCall rows
    Repo-->>Svc: Page<MissedCall>
    Svc-->>DashCtrl: PagedResponse<MissedCallResponse>
    DashCtrl->>Svc: getTotalCount(), getTodayCount(), getLatest()
    Svc-->>DashCtrl: counts + latest list
    DashCtrl->>View: model attributes (missedCalls, counts, pagination)
    View-->>Admin: Rendered HTML dashboard
```

---

## 4. Call Flow Explanation

1. A call lands on the Exotel Exoline/virtual number and is routed through the configured Exotel App Bazaar flow.
2. If the call is **not answered, busy, fails, or is canceled**, Exotel's call-status callback (or Passthru applet) fires a webhook to this application's `/api/exotel/webhook` endpoint with the call's metadata.
3. The webhook controller extracts `CallSid`, `From`, `Status`, and `StartTime`/`EndTime` from the form payload.
4. If the status is not one of the "missed" types (e.g., it was actually answered), the event is acknowledged but discarded.
5. The service layer checks `call_sid` uniqueness to prevent duplicate inserts from Exotel's automatic webhook retries.
6. The caller's phone number is looked up in the `callers` table; if found, the registered name is attached, otherwise `"Unknown"` is stored.
7. The missed call record is persisted to MySQL.
8. The data is immediately queryable via REST APIs and visible on the live dashboard.
