package com.exotel.missedcalls.service;

import com.exotel.missedcalls.entity.Caller;

import java.util.Optional;

public interface CallerService {

    /**
     * Resolves a caller's name from the callers table.
     * Returns "Unknown" when not found.
     */
    String resolveCallerName(String phoneNumber);

    Optional<Caller> findByPhoneNumber(String phoneNumber);

    Caller save(Caller caller);
}
