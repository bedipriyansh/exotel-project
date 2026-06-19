package com.exotel.missedcalls.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response representation of a missed call returned by the REST API
 * and rendered on the dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissedCallResponse {

    private Long id;
    private String callerNumber;
    private String callerName;
    private String callSid;
    private String callStatus;
    private LocalDateTime missedCallTime;
    private LocalDateTime createdAt;
}
