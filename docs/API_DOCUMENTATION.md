# API Documentation — Exotel Missed Call Tracking System

Base URL: `http://localhost:8080`
Interactive Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON: `http://localhost:8080/api-docs`

All REST responses (except the dashboard HTML and the raw webhook from Exotel) are wrapped as:
```json
{
  "success": true,
  "message": "...",
  "data": { },
  "timestamp": "2026-06-19T10:30:00"
}
```

---

## 1. Webhook Endpoint

### POST `/api/exotel/webhook`
Receives call status callbacks from Exotel. See `sample-webhook-payloads.md` for full payload examples.

**Request (form-urlencoded):**
```
CallSid=CA1234567890abcdef1234567890abcd
CallFrom=+919876543210
Status=no-answer
StartTime=2026-06-19 10:15:32
```

**Response — 201 Created (missed call saved):**
```json
{
  "success": true,
  "message": "Missed call recorded successfully",
  "data": {
    "id": 101,
    "callerNumber": "+919876543210",
    "callerName": "Rahul Sharma",
    "callSid": "CA1234567890abcdef1234567890abcd",
    "callStatus": "no-answer",
    "missedCallTime": "2026-06-19T10:15:32",
    "createdAt": "2026-06-19T10:15:33.102"
  },
  "timestamp": "2026-06-19T10:15:33.150"
}
```

**Response — 200 OK (non-missed status, ignored):**
```json
{ "success": true, "message": "Event received but not processed (not a missed call status)" }
```

**Response — 200 OK (duplicate CallSid):**
```json
{ "success": true, "message": "Call SID already processed: CA1234567890abcdef1234567890abcd" }
```

**Response — 400 Bad Request (missing required field):**
```json
{
  "success": false,
  "status": 400,
  "error": "Bad Request",
  "message": "CallSid and From are required fields in the Exotel webhook payload",
  "path": "/api/exotel/webhook",
  "timestamp": "2026-06-19T10:15:33.150"
}
```

### GET `/api/exotel/webhook`
Health check to confirm the webhook URL is reachable from Exotel's network.

---

## 2. Missed Call APIs

### GET `/api/missed-calls`
Paginated list of all missed calls, sorted by most recent first.

Query params: `page` (default 0), `size` (default 20), `sort`

**Sample:** `GET /api/missed-calls?page=0&size=10`

**Response:**
```json
{
  "success": true,
  "message": "Missed calls fetched successfully",
  "data": {
    "content": [
      {
        "id": 101,
        "callerNumber": "+919876543210",
        "callerName": "Rahul Sharma",
        "callSid": "CA1234567890abcdef1234567890abcd",
        "callStatus": "no-answer",
        "missedCallTime": "2026-06-19T10:15:32",
        "createdAt": "2026-06-19T10:15:33.102"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2026-06-19T10:20:00"
}
```

### GET `/api/missed-calls/{id}`
Fetch a single missed call by its database ID.

**Sample:** `GET /api/missed-calls/101`
**404 Response (not found):**
```json
{ "success": false, "status": 404, "error": "Not Found",
  "message": "Missed call not found with id: 999", "path": "/api/missed-calls/999" }
```

### GET `/api/missed-calls/number/{phoneNumber}`
Search missed calls by partial/full phone number.

**Sample:** `GET /api/missed-calls/number/9876543210`

### GET `/api/missed-calls/today`
Returns missed calls that occurred today (server local date).

### GET `/api/missed-calls/count`
**Response:** `{ "success": true, "message": "...", "data": 42 }`

### GET `/api/missed-calls/latest`
Returns the 10 most recent missed calls (no pagination wrapper).

### GET `/api/missed-calls/search?phoneNumber=&fromDate=&toDate=`
Combined filter used internally by the dashboard; also usable directly.

**Sample:** `GET /api/missed-calls/search?phoneNumber=98765&fromDate=2026-06-01&toDate=2026-06-19`

---

## 3. Dashboard

### GET `/dashboard`
Server-rendered Thymeleaf page showing:
- Total missed calls, today's count, latest records (summary cards)
- Filterable, paginated table (phone number + date range)

Query params: `phoneNumber`, `fromDate`, `toDate`, `page`, `size`

---

## 4. Error Handling Strategy

| Scenario                          | HTTP Status | Behavior |
|-----------------------------------|--------------|----------|
| Missing CallSid/From in webhook   | 400          | Logged as warning, Exotel can be configured to retry |
| Duplicate CallSid                 | 200          | Silently ignored — prevents Exotel retry storms |
| Status not a missed-call type     | 200          | Acknowledged, not persisted |
| Record not found (GET by id)      | 404          | Standard error body |
| Unexpected/internal error         | 500          | Logged with stack trace, generic message returned to caller |
| Validation failure on query params| 400          | Field-level messages returned in `details` |

All exceptions are funneled through a single `@RestControllerAdvice` (`GlobalExceptionHandler`) to guarantee
a consistent JSON error shape across the API.
