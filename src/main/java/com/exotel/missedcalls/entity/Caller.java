package com.exotel.missedcalls.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a registered caller / contact.
 * Used to resolve a friendly name for an incoming phone number.
 */
@Entity
@Table(name = "callers", indexes = {
        @Index(name = "idx_callers_phone_number", columnList = "phone_number", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Caller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
