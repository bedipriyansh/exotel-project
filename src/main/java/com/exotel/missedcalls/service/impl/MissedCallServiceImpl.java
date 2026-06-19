package com.exotel.missedcalls.service.impl;

import com.exotel.missedcalls.dto.ExotelWebhookRequest;
import com.exotel.missedcalls.dto.MissedCallResponse;
import com.exotel.missedcalls.dto.PagedResponse;
import com.exotel.missedcalls.dto.MissedCallAggregateResponse;
import com.exotel.missedcalls.entity.CallStatus;
import com.exotel.missedcalls.entity.MissedCall;
import com.exotel.missedcalls.exception.DuplicateCallSidException;
import com.exotel.missedcalls.exception.InvalidWebhookPayloadException;
import com.exotel.missedcalls.exception.ResourceNotFoundException;
import com.exotel.missedcalls.repository.MissedCallRepository;
import com.exotel.missedcalls.service.CallerService;
import com.exotel.missedcalls.service.MissedCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core business logic for processing Exotel webhook events and serving
 * missed call data to the REST API / dashboard.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MissedCallServiceImpl implements MissedCallService {

    private final MissedCallRepository missedCallRepository;
    private final CallerService callerService;

    // Exotel typically sends timestamps like "2024-01-15 10:23:45"
    private static final List<DateTimeFormatter> SUPPORTED_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
    );

    @jakarta.annotation.PostConstruct
    @Transactional
    public void init() {
        log.info("Normalizing destination numbers for existing database records...");
        try {
            missedCallRepository.normalizeDestinationNumbers();
        } catch (Exception e) {
            log.error("Failed to normalize destination numbers on startup", e);
        }
    }

    @Override
    @Transactional
    public MissedCallResponse processWebhookEvent(ExotelWebhookRequest request) {
        validate(request);

        // Idempotency check: prevent duplicate records for the same Call SID.
        if (missedCallRepository.existsByCallSid(request.getCallSid())) {
            log.info("Duplicate webhook received for CallSid={}. Skipping insert.", request.getCallSid());
            throw new DuplicateCallSidException("Call SID already processed: " + request.getCallSid());
        }

        String normalizedNumber = normalizePhoneNumber(request.getFrom());
        String callerName = callerService.resolveCallerName(normalizedNumber);
        String destinationNumber = StringUtils.hasText(request.getTo()) ? normalizePhoneNumber(request.getTo()) : "Unknown";
        CallStatus status = CallStatus.fromString(request.getStatus());
        LocalDateTime missedTime = resolveMissedCallTime(request);

        MissedCall missedCall = MissedCall.builder()
                .callerNumber(normalizedNumber)
                .callerName(callerName)
                .destinationNumber(destinationNumber)
                .callSid(request.getCallSid())
                .callStatus(status)
                .missedCallTime(missedTime)
                .build();

        MissedCall saved = missedCallRepository.save(missedCall);
        log.info("Missed call saved. id={}, callSid={}, number={}, status={}",
                saved.getId(), saved.getCallSid(), saved.getCallerNumber(), saved.getCallStatus());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MissedCallResponse> getAllMissedCalls(Pageable pageable) {
        Page<MissedCall> page = missedCallRepository.findAllByOrderByMissedCallTimeDesc(pageable);
        return PagedResponse.from(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public MissedCallResponse getById(Long id) {
        MissedCall missedCall = missedCallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Missed call not found with id: " + id));
        return toResponse(missedCall);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MissedCallResponse> searchByPhoneNumber(String phoneNumber, Pageable pageable) {
        Page<MissedCall> page = missedCallRepository.findByCallerNumberContaining(phoneNumber, pageable);
        return PagedResponse.from(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MissedCallResponse> getTodaysMissedCalls(Pageable pageable) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        Page<MissedCall> page = missedCallRepository.findByMissedCallTimeBetween(startOfDay, endOfDay, pageable);
        return PagedResponse.from(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MissedCallResponse> search(String phoneNumber, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.plusDays(1).atStartOfDay().minusNanos(1) : null;
        String normalizedPhone = StringUtils.hasText(phoneNumber) ? phoneNumber.trim() : null;
        Page<MissedCall> page = missedCallRepository.search(normalizedPhone, start, end, pageable);
        return PagedResponse.from(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return missedCallRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTodayCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        return missedCallRepository.countByMissedCallTimeBetween(startOfDay, endOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissedCallResponse> getLatest() {
        return missedCallRepository.findTop10ByOrderByMissedCallTimeDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissedCallAggregateResponse> getMissedCallAggregates() {
        return missedCallRepository.getMissedCallAggregates();
    }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    private void validate(ExotelWebhookRequest request) {
        if (!StringUtils.hasText(request.getCallSid())) {
            throw new InvalidWebhookPayloadException("CallSid is missing in webhook payload");
        }
        if (!StringUtils.hasText(request.getFrom())) {
            throw new InvalidWebhookPayloadException("Caller number (From) is missing in webhook payload");
        }
    }

    private String normalizePhoneNumber(String rawNumber) {
        // Strip whitespace; keep leading '+' if present (E.164 style numbers).
        String trimmed = rawNumber.trim();
        return trimmed.replaceAll("[^+\\d]", "");
    }

    private LocalDateTime resolveMissedCallTime(ExotelWebhookRequest request) {
        String candidate = StringUtils.hasText(request.getStartTime())
                ? request.getStartTime()
                : request.getEndTime();

        if (!StringUtils.hasText(candidate)) {
            log.warn("No StartTime/EndTime supplied for CallSid={}. Using current server time.", request.getCallSid());
            return LocalDateTime.now();
        }

        for (DateTimeFormatter formatter : SUPPORTED_FORMATS) {
            try {
                return LocalDateTime.parse(candidate.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                // try next formatter
            }
        }

        log.warn("Unable to parse timestamp '{}' for CallSid={}. Using current server time.", candidate, request.getCallSid());
        return LocalDateTime.now();
    }

    private MissedCallResponse toResponse(MissedCall entity) {
        return MissedCallResponse.builder()
                .id(entity.getId())
                .callerNumber(entity.getCallerNumber())
                .callerName(entity.getCallerName())
                .destinationNumber(entity.getDestinationNumber())
                .callSid(entity.getCallSid())
                .callStatus(entity.getCallStatus().getExotelValue())
                .missedCallTime(entity.getMissedCallTime())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
