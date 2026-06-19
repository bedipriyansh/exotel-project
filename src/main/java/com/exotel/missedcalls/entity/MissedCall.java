package com.exotel.missedcalls.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a single missed call event received from Exotel.
 * call_sid is unique to prevent duplicate webhook deliveries from
 * creating duplicate records.
 */
@Entity
@Table(name = "missed_calls", indexes = {
        @Index(name = "idx_missed_calls_call_sid", columnList = "call_sid", unique = true),
        @Index(name = "idx_missed_calls_caller_number", columnList = "caller_number"),
        @Index(name = "idx_missed_calls_missed_call_time", columnList = "missed_call_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissedCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caller_number", nullable = false, length = 20)
    private String callerNumber;

    @Column(name = "caller_name", nullable = false, length = 150)
    private String callerName;

    @Column(name = "destination_number", nullable = false, length = 20)
    private String destinationNumber;

    @Column(name = "call_sid", nullable = false, unique = true, length = 100)
    private String callSid;

    @Enumerated(EnumType.STRING)
    @Column(name = "call_status", nullable = false, length = 30)
    private CallStatus callStatus;

    @Column(name = "missed_call_time", nullable = false)
    private LocalDateTime missedCallTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
