package com.exotel.missedcalls.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the payload Exotel sends to the webhook endpoint when a
 * call event occurs (passthru / status callback).
 *
 * Exotel sends form-url-encoded POST data by default; field names below
 * are mapped via JsonAlias to support both the standard Exotel field names
 * and JSON-style payloads (e.g., when Exotel "Passthru applet" / custom
 * webhook is configured to send JSON).
 *
 * Common Exotel fields:
 *  - CallSid          : Unique identifier for the call
 *  - CallFrom / From  : Caller's phone number
 *  - CallTo / To      : Destination (Exoline / Exotel number)
 *  - Status / DialCallStatus : Status of the call (completed, no-answer, busy, failed, canceled)
 *  - StartTime        : Call start timestamp
 *  - EndTime          : Call end timestamp
 *  - Direction         : inbound / outbound
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExotelWebhookRequest {

    @NotBlank(message = "CallSid is required")
    @JsonAlias({"CallSid", "call_sid", "Sid"})
    private String callSid;

    @NotBlank(message = "Caller number (From) is required")
    @JsonAlias({"CallFrom", "From", "from", "caller_number"})
    private String from;

    @JsonAlias({"CallTo", "To", "to"})
    private String to;

    @JsonAlias({"Status", "DialCallStatus", "CallStatus", "call_status", "status"})
    private String status;

    @JsonAlias({"StartTime", "start_time"})
    private String startTime;

    @JsonAlias({"EndTime", "end_time"})
    private String endTime;

    @JsonAlias({"Direction", "direction"})
    private String direction;

    @JsonAlias({"CallType", "call_type"})
    private String callType;
}
