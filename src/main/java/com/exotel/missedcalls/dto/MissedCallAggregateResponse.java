package com.exotel.missedcalls.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to represent the route summary count.
 * Displays how many missed calls were made from a particular caller number
 * to a particular destination (virtual) number.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissedCallAggregateResponse {
    private String callerNumber;
    private String callerName;
    private String destinationNumber;
    private Long missedCallCount;
}
