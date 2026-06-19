package com.exotel.missedcalls.controller;

import com.exotel.missedcalls.dto.ApiResponse;
import com.exotel.missedcalls.dto.ExotelWebhookRequest;
import com.exotel.missedcalls.dto.MissedCallResponse;
import com.exotel.missedcalls.entity.CallStatus;
import com.exotel.missedcalls.exception.DuplicateCallSidException;
import com.exotel.missedcalls.exception.InvalidWebhookPayloadException;
import com.exotel.missedcalls.service.MissedCallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Receives real-time call event webhooks from Exotel.
 *
 * Exotel's "Passthru Applet" / status callback posts data as
 * application/x-www-form-urlencoded with PascalCase field names
 * (CallSid, From, Status, StartTime, ...). Because these names do not
 * match Java's camelCase bean-property convention, Spring's standard
 * {@code @ModelAttribute} binding (which is case-sensitive) cannot be
 * relied on. Instead, this controller reads the raw request parameters
 * directly and resolves each field case-insensitively, which works for
 * both form-urlencoded and query-string deliveries. The endpoint always
 * responds with HTTP 200/201 for successfully handled or intentionally
 * ignored events so Exotel does not treat the call flow as failed or
 * retry indefinitely.
 */
@Slf4j
@RestController
@RequestMapping("/api/exotel")
@RequiredArgsConstructor
@Tag(name = "Exotel Webhook", description = "Endpoint that receives missed call events from Exotel")
public class ExotelWebhookController {

    private final MissedCallService missedCallService;

    @Operation(summary = "Receive Exotel call event webhook",
            description = "Accepts call status callbacks from Exotel (form-urlencoded or query parameters). " +
                    "Persists the event only when the call status indicates a missed call " +
                    "(no-answer, busy, failed, canceled). Duplicate CallSid values are ignored.")
    @PostMapping(value = "/webhook")
    public ResponseEntity<ApiResponse<MissedCallResponse>> receiveWebhook(HttpServletRequest httpRequest) {
        ExotelWebhookRequest request = parseRequest(httpRequest);

        log.info("Received Exotel webhook: CallSid={}, From={}, Status={}",
                request.getCallSid(), request.getFrom(), request.getStatus());

        if (!StringUtils.hasText(request.getCallSid()) || !StringUtils.hasText(request.getFrom())) {
            throw new InvalidWebhookPayloadException("CallSid and From are required fields in the Exotel webhook payload");
        }

        if (!CallStatus.isMissed(request.getStatus())) {
            log.info("Call status '{}' for CallSid={} is not a missed-call status. Ignoring.",
                    request.getStatus(), request.getCallSid());
            return ResponseEntity.ok(ApiResponse.success(
                    "Event received but not processed (not a missed call status)", null));
        }

        try {
            MissedCallResponse response = missedCallService.processWebhookEvent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Missed call recorded successfully", response));
        } catch (DuplicateCallSidException ex) {
            return ResponseEntity.ok(ApiResponse.success(ex.getMessage(), null));
        }
    }

    @Operation(summary = "Webhook health check",
            description = "Simple GET endpoint Exotel/admins can use to verify the webhook URL is reachable.")
    @GetMapping("/webhook")
    public ResponseEntity<ApiResponse<String>> webhookHealthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Webhook endpoint is reachable", "OK"));
    }

    /**
     * Builds an {@link ExotelWebhookRequest} from raw HTTP parameters,
     * matching Exotel's field names case-insensitively (CallSid, callsid,
     * call_sid, etc. are all accepted).
     */
    private ExotelWebhookRequest parseRequest(HttpServletRequest httpRequest) {
        Map<String, String[]> params = httpRequest.getParameterMap();

        return new ExotelWebhookRequest(
                firstMatch(params, "CallSid", "call_sid", "Sid"),
                firstMatch(params, "CallFrom", "From", "from", "caller_number"),
                firstMatch(params, "CallTo", "To", "to"),
                firstMatch(params, "Status", "DialCallStatus", "CallStatus", "call_status", "status"),
                firstMatch(params, "StartTime", "start_time"),
                firstMatch(params, "EndTime", "end_time"),
                firstMatch(params, "Direction", "direction"),
                firstMatch(params, "CallType", "call_type")
        );
    }

    private String firstMatch(Map<String, String[]> params, String... candidateNames) {
        for (String candidate : candidateNames) {
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(candidate)
                        && entry.getValue() != null
                        && entry.getValue().length > 0) {
                    return entry.getValue()[0];
                }
            }
        }
        return null;
    }
}
