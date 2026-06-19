package com.exotel.missedcalls.service.impl;

import com.exotel.missedcalls.entity.Caller;
import com.exotel.missedcalls.repository.CallerRepository;
import com.exotel.missedcalls.service.CallerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallerServiceImpl implements CallerService {

    private static final String UNKNOWN_CALLER = "Unknown";

    private final CallerRepository callerRepository;

    @Override
    @Transactional(readOnly = true)
    public String resolveCallerName(String phoneNumber) {
        return callerRepository.findByPhoneNumber(phoneNumber)
                .map(Caller::getName)
                .orElseGet(() -> {
                    log.debug("No registered caller found for number {}. Defaulting to '{}'.", phoneNumber, UNKNOWN_CALLER);
                    return UNKNOWN_CALLER;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Caller> findByPhoneNumber(String phoneNumber) {
        return callerRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    @Transactional
    public Caller save(Caller caller) {
        return callerRepository.save(caller);
    }
}
