package com.exotel.missedcalls.controller;

import com.exotel.missedcalls.dto.ApiResponse;
import com.exotel.missedcalls.dto.MissedCallResponse;
import com.exotel.missedcalls.dto.PagedResponse;
import com.exotel.missedcalls.service.MissedCallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST APIs for retrieving missed call records.
 */
@Slf4j
@RestController
@RequestMapping("/api/missed-calls")
@RequiredArgsConstructor
@Tag(name = "Missed Calls", description = "APIs to retrieve missed call records")
public class MissedCallController {

    private final MissedCallService missedCallService;

    @Operation(summary = "Get all missed calls (paginated)")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MissedCallResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<MissedCallResponse> result = missedCallService.getAllMissedCalls(pageable);
        return ResponseEntity.ok(ApiResponse.success("Missed calls fetched successfully", result));
    }

    @Operation(summary = "Get a missed call by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MissedCallResponse>> getById(@PathVariable Long id) {
        MissedCallResponse result = missedCallService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Missed call fetched successfully", result));
    }

    @Operation(summary = "Search missed calls by (partial) phone number")
    @GetMapping("/number/{phoneNumber}")
    public ResponseEntity<ApiResponse<PagedResponse<MissedCallResponse>>> getByPhoneNumber(
            @PathVariable String phoneNumber,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<MissedCallResponse> result = missedCallService.searchByPhoneNumber(phoneNumber, pageable);
        return ResponseEntity.ok(ApiResponse.success("Missed calls fetched successfully", result));
    }

    @Operation(summary = "Get today's missed calls")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<PagedResponse<MissedCallResponse>>> getToday(
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<MissedCallResponse> result = missedCallService.getTodaysMissedCalls(pageable);
        return ResponseEntity.ok(ApiResponse.success("Today's missed calls fetched successfully", result));
    }

    @Operation(summary = "Get the total count of all missed calls")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getCount() {
        long count = missedCallService.getTotalCount();
        return ResponseEntity.ok(ApiResponse.success("Total missed call count fetched successfully", count));
    }

    @Operation(summary = "Get the 10 most recent missed calls")
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<MissedCallResponse>>> getLatest() {
        List<MissedCallResponse> result = missedCallService.getLatest();
        return ResponseEntity.ok(ApiResponse.success("Latest missed calls fetched successfully", result));
    }

    @Operation(summary = "Advanced search by phone number and/or date range")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<MissedCallResponse>>> search(
            @Parameter(description = "Phone number, partial match allowed") @RequestParam(required = false) String phoneNumber,
            @Parameter(description = "From date (yyyy-MM-dd)") @RequestParam(required = false) java.time.LocalDate fromDate,
            @Parameter(description = "To date (yyyy-MM-dd)") @RequestParam(required = false) java.time.LocalDate toDate,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<MissedCallResponse> result = missedCallService.search(phoneNumber, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search results fetched successfully", result));
    }
}
