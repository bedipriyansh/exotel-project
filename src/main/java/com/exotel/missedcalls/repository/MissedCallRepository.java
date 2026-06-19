package com.exotel.missedcalls.repository;

import com.exotel.missedcalls.entity.MissedCall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.exotel.missedcalls.dto.MissedCallAggregateResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MissedCallRepository extends JpaRepository<MissedCall, Long> {

    Optional<MissedCall> findByCallSid(String callSid);

    boolean existsByCallSid(String callSid);

    Page<MissedCall> findByCallerNumberContaining(String phoneNumber, Pageable pageable);

    Page<MissedCall> findByMissedCallTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT m FROM MissedCall m WHERE " +
            "(:phoneNumber IS NULL OR m.callerNumber LIKE CONCAT('%', :phoneNumber, '%')) AND " +
            "(:start IS NULL OR m.missedCallTime >= :start) AND " +
            "(:end IS NULL OR m.missedCallTime <= :end)")
    Page<MissedCall> search(@Param("phoneNumber") String phoneNumber,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             Pageable pageable);

    long countByMissedCallTimeBetween(LocalDateTime start, LocalDateTime end);

    List<MissedCall> findTop10ByOrderByMissedCallTimeDesc();

    Page<MissedCall> findAllByOrderByMissedCallTimeDesc(Pageable pageable);

    @Query("SELECT new com.exotel.missedcalls.dto.MissedCallAggregateResponse(" +
           "m.callerNumber, m.callerName, m.destinationNumber, COUNT(m)) " +
           "FROM MissedCall m " +
           "GROUP BY m.callerNumber, m.callerName, m.destinationNumber " +
           "ORDER BY COUNT(m) DESC")
    List<MissedCallAggregateResponse> getMissedCallAggregates();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE MissedCall m SET m.destinationNumber = 'Unknown' WHERE m.destinationNumber IS NULL OR m.destinationNumber = ''")
    void normalizeDestinationNumbers();
}
