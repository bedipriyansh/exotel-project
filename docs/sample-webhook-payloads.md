# Sample Exotel Webhook Payloads

Exotel call status callbacks (configured under **Exotel App Bazaar / Call Flow → Passthru Applet** or
the number's **"Call Status Callback URL"**) are delivered as `application/x-www-form-urlencoded`
POST requests. Below are representative payloads for each scenario this system handles.

> Webhook URL to configure in Exotel: `https://your-domain.com/api/exotel/webhook`

---

## 1. Missed Call — No Answer

```
CallSid=CA1234567890abcdef1234567890abcd
CallFrom=+919876543210
CallTo=+912233445566
Direction=inbound
Status=no-answer
StartTime=2026-06-19 10:15:32
EndTime=2026-06-19 10:15:47
CallType=call-attempt
```

Equivalent curl request:
```bash
curl -X POST https://your-domain.com/api/exotel/webhook \
  -d "CallSid=CA1234567890abcdef1234567890abcd" \
  -d "CallFrom=+919876543210" \
  -d "CallTo=+912233445566" \
  -d "Status=no-answer" \
  -d "StartTime=2026-06-19 10:15:32" \
  -d "EndTime=2026-06-19 10:15:47"
```

## 2. Missed Call — Busy

```
CallSid=CA2234567890abcdef1234567890abcd
CallFrom=+919812345678
CallTo=+912233445566
Status=busy
StartTime=2026-06-19 11:02:10
EndTime=2026-06-19 11:02:12
```

## 3. Missed Call — Failed

```
CallSid=CA3234567890abcdef1234567890abcd
CallFrom=+919900112233
CallTo=+912233445566
Status=failed
StartTime=2026-06-19 12:45:00
EndTime=2026-06-19 12:45:05
```

## 4. Missed Call — Canceled

```
CallSid=CA4234567890abcdef1234567890abcd
CallFrom=+919988776655
CallTo=+912233445566
Status=canceled
StartTime=2026-06-19 13:30:00
EndTime=2026-06-19 13:30:02
```

## 5. Answered Call (NOT stored — ignored by design)

```
CallSid=CA5234567890abcdef1234567890abcd
CallFrom=+919876543210
CallTo=+912233445566
Status=completed
StartTime=2026-06-19 14:00:00
EndTime=2026-06-19 14:05:30
```
Response: `200 OK` with message `"Event received but not processed (not a missed call status)"`
— no row is written to `missed_calls`.

## 6. Duplicate CallSid (Exotel retry / re-delivery)

Sending payload #1 again returns:
```json
{
  "success": true,
  "message": "Call SID already processed: CA1234567890abcdef1234567890abcd",
  "timestamp": "2026-06-19T10:15:50"
}
```
HTTP Status: `200 OK` (intentionally, so Exotel does not keep retrying).

---

## Field Reference

| Exotel Field        | Meaning                                  | Mapped To                  |
|----------------------|-------------------------------------------|-----------------------------|
| `CallSid`            | Unique call identifier                    | `missed_calls.call_sid`     |
| `CallFrom` / `From`  | Caller's phone number                     | `missed_calls.caller_number`|
| `CallTo` / `To`      | Exotel/Exoline number dialed              | (not stored, available for extension) |
| `Status`             | Call outcome (no-answer/busy/failed/canceled/completed) | `missed_calls.call_status` |
| `StartTime`          | When the call attempt started             | `missed_calls.missed_call_time` |
| `EndTime`            | When the call attempt ended (fallback)    | used if `StartTime` absent  |
| `Direction`          | inbound / outbound                        | (not stored, available for extension) |
