package com.exotel.missedcalls.repository;

import com.exotel.missedcalls.entity.Caller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CallerRepository extends JpaRepository<Caller, Long> {

    Optional<Caller> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
}
