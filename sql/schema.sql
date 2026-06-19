-- ============================================================
-- Exotel Missed Call Tracking System - MySQL Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS exotel_missed_calls
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE exotel_missed_calls;

-- ------------------------------------------------------------
-- Table: callers
-- Stores registered contacts so we can resolve a friendly name
-- for an incoming phone number.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS callers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number    VARCHAR(20)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_callers_phone_number UNIQUE (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Table: missed_calls
-- Stores every missed call event received via the Exotel webhook.
-- call_sid is unique to prevent duplicate inserts on webhook retries.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS missed_calls (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    caller_number     VARCHAR(20)  NOT NULL,
    caller_name       VARCHAR(150) NOT NULL,
    destination_number VARCHAR(20)  NOT NULL DEFAULT 'Unknown',
    call_sid          VARCHAR(100) NOT NULL,
    call_status       VARCHAR(30)  NOT NULL,
    missed_call_time  DATETIME(6)  NOT NULL,
    created_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_missed_calls_call_sid UNIQUE (call_sid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes to speed up common lookups
CREATE INDEX idx_missed_calls_caller_number ON missed_calls (caller_number);
CREATE INDEX idx_missed_calls_destination_number ON missed_calls (destination_number);
CREATE INDEX idx_missed_calls_missed_call_time ON missed_calls (missed_call_time);

-- ------------------------------------------------------------
-- Sample seed data (optional - for local testing)
-- ------------------------------------------------------------
INSERT INTO callers (phone_number, name) VALUES
    ('+919876543210', 'Rahul Sharma'),
    ('+919812345678', 'Priya Verma'),
    ('+919900112233', 'Amit Kumar')
ON DUPLICATE KEY UPDATE name = VALUES(name);
