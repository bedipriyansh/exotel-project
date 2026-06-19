package com.exotel.missedcalls.service;

import com.exotel.missedcalls.dto.ExotelWebhookRequest;
import com.exotel.missedcalls.dto.MissedCallResponse;
import com.exotel.missedcalls.dto.PagedResponse;
import com.exotel.missedcalls.dto.MissedCallAggregateResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface MissedCallService {

    /**
     * Processes an incoming Exotel webhook event. Persists the missed call
     * record if the status indicates a missed call and the call_sid has
     * not already been processed.
     *
     * @return the saved record's response representation
     */
    MissedCallResponse processWebhookEvent(ExotelWebhookRequest request);

    PagedResponse<MissedCallResponse> getAllMissedCalls(Pageable pageable);

    MissedCallResponse getById(Long id);

    PagedResponse<MissedCallResponse> searchByPhoneNumber(String phoneNumber, Pageable pageable);

    PagedResponse<MissedCallResponse> getTodaysMissedCalls(Pageable pageable);

    PagedResponse<MissedCallResponse> search(String phoneNumber, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    long getTotalCount();

    long getTodayCount();

    java.util.List<MissedCallResponse> getLatest();

    java.util.List<MissedCallAggregateResponse> getMissedCallAggregates();
}
