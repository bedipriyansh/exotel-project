# Exotel Missed Call Tracking System

A production-quality Spring Boot application that receives real-time missed-call webhooks from
Exotel, stores them in MySQL, exposes REST APIs, and shows them on a live Thymeleaf dashboard.

## Tech Stack
- Java 21, Spring Boot 3.3.x
- Spring Web, Spring Data JPA (Hibernate), Spring Validation
- MySQL 8.0
- Thymeleaf (dashboard UI)
- springdoc-openapi (Swagger UI)
- Maven

## Project Structure
```
exotel-missed-call/
├── pom.xml
├── sql/
│   ├── schema.sql              # CREATE DATABASE / CREATE TABLE scripts
│   └── sample_data.sql         # Optional seed data
├── docs/
│   ├── API_DOCUMENTATION.md
│   ├── ARCHITECTURE_AND_DIAGRAMS.md
│   └── sample-webhook-payloads.md
└── src/main/java/com/exotel/missedcalls/
    ├── ExotelMissedCallApplication.java
    ├── config/          # SwaggerConfig, WebConfig (CORS + request logging)
    ├── controller/       # ExotelWebhookController, MissedCallController, DashboardController
    ├── service/           # interfaces
    │   └── impl/          # MissedCallServiceImpl, CallerServiceImpl
    ├── repository/        # MissedCallRepository, CallerRepository
    ├── entity/            # MissedCall, Caller, CallStatus (enum)
    ├── dto/                # ExotelWebhookRequest, MissedCallResponse, ApiResponse, PagedResponse
    └── exception/          # GlobalExceptionHandler + custom exceptions
    src/main/resources/
    ├── application.properties
    ├── templates/dashboard.html
    └── static/css/dashboard.css
```

## 1. Prerequisites
- JDK 21+
- Maven 3.8+
- MySQL 8.0 running locally (or accessible host)
- (Optional) An Exotel account + a public HTTPS URL (use ngrok for local testing)

## 2. Database Setup
```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/sample_data.sql   # optional sample data
```
This creates the `exotel_missed_calls` database with `callers` and `missed_calls` tables.

## 3. Configure Application
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/exotel_missed_calls?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```
(`spring.jpa.hibernate.ddl-auto=update` is already set, so Hibernate will keep the schema in sync
with the entities — running `schema.sql` first is still recommended for a clean initial setup.)

## 4. Build & Run
```bash
mvn clean install
mvn spring-boot:run
```
Or run the packaged jar:
```bash
mvn clean package
java -jar target/exotel-missed-call-tracker-1.0.0.jar
```
The app starts on **http://localhost:8080**.

## 5. Verify
- Dashboard: http://localhost:8080/dashboard
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI spec: http://localhost:8080/api-docs
- Webhook health check: `GET http://localhost:8080/api/exotel/webhook`

## 6. Test the Webhook Locally (without a real Exotel call)
```bash
curl -X POST http://localhost:8080/api/exotel/webhook \
  -d "CallSid=CA_TEST_0001" \
  -d "CallFrom=+919876543210" \
  -d "Status=no-answer" \
  -d "StartTime=2026-06-19 10:15:32"
```
Then check:
```bash
curl http://localhost:8080/api/missed-calls/latest
```
or refresh http://localhost:8080/dashboard.

## 7. Exotel Configuration (Production)
1. Expose your app over HTTPS (e.g., deploy it, or use `ngrok http 8080` for testing).
2. In the Exotel dashboard, open your **Number → Call Flow / App Bazaar**.
3. Add a **Passthru Applet** (or set the **Status Callback URL**) pointing to:
   `https://<your-domain>/api/exotel/webhook`
4. Set the HTTP method to **POST**.
5. Make a test call to the Exotel number and let it go unanswered — the missed call should appear
   on the dashboard within seconds.

See `docs/sample-webhook-payloads.md` for exact payload formats and
`docs/ARCHITECTURE_AND_DIAGRAMS.md` for sequence/architecture diagrams.

## 8. Key Design Decisions
- **Idempotency**: `call_sid` has a unique constraint at the DB level and is checked in the service
  layer before insert, so Exotel's webhook retries never create duplicate rows.
- **Case-insensitive webhook parsing**: Exotel sends PascalCase form fields (`CallSid`, `From`).
  Rather than relying on case-sensitive Spring `@ModelAttribute` binding, the controller reads raw
  HTTP parameters and matches field names case-insensitively — this is more robust against Exotel
  configuration variations.
- **Always return 2xx to Exotel**: Even "ignored" events (duplicates, non-missed statuses) return
  HTTP 200/201 so Exotel does not endlessly retry the webhook.
- **Unknown caller fallback**: If the incoming number isn't in the `callers` table, `caller_name`
  is stored as `"Unknown"` rather than left null, simplifying downstream display logic.
- **Layered architecture**: Controllers contain no business logic; all business rules live in the
  service layer, which is fully unit-testable independent of HTTP/JPA concerns.

## 9. Extending the System
- Add authentication (Spring Security) to protect `/dashboard` and write APIs.
- Add a `notes`/`call_back_status` column to track follow-up actions on missed calls.
- Publish events to a message queue for SMS/email alerts on missed calls.
- Add Flyway/Liquibase for versioned schema migrations instead of `ddl-auto=update`.
