package com.exotel.missedcalls.entity;

/**
 * Represents the status of a call as reported by Exotel.
 * Exotel typically sends values like: no-answer, busy, failed, canceled, completed.
 */
public enum CallStatus {
    NO_ANSWER("no-answer"),
    BUSY("busy"),
    FAILED("failed"),
    CANCELED("canceled"),
    COMPLETED("completed"),
    UNKNOWN("unknown");

    private final String exotelValue;

    CallStatus(String exotelValue) {
        this.exotelValue = exotelValue;
    }

    public String getExotelValue() {
        return exotelValue;
    }

    /**
     * Parses an incoming Exotel status string into the enum,
     * defaulting to UNKNOWN when not recognized.
     */
    public static CallStatus fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        String normalized = value.trim().toLowerCase().replace("_", "-");
        for (CallStatus status : values()) {
            if (status.exotelValue.equals(normalized)) {
                return status;
            }
        }
        return UNKNOWN;
    }

    /**
     * Determines whether a given status string represents a "missed call"
     * scenario that should be persisted by this system.
     */
    public static boolean isMissed(String value) {
        CallStatus status = fromString(value);
        return status == NO_ANSWER || status == BUSY || status == FAILED || status == CANCELED;
    }
}
