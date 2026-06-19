package com.exotel.missedcalls;

import com.exotel.missedcalls.dto.ExotelWebhookRequest;
import com.exotel.missedcalls.dto.MissedCallResponse;
import com.exotel.missedcalls.entity.CallStatus;
import com.exotel.missedcalls.entity.MissedCall;
import com.exotel.missedcalls.exception.DuplicateCallSidException;
import com.exotel.missedcalls.repository.MissedCallRepository;
import com.exotel.missedcalls.service.CallerService;
import com.exotel.missedcalls.service.impl.MissedCallServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissedCallServiceImplTest {

    @Mock
    private MissedCallRepository missedCallRepository;

    @Mock
    private CallerService callerService;

    @InjectMocks
    private MissedCallServiceImpl missedCallService;

    @Test
    void processWebhookEvent_savesNewMissedCall() {
        ExotelWebhookRequest request = new ExotelWebhookRequest(
                "CA_TEST_001", "+919876543210", "+912233445566",
                "no-answer", "2026-06-19 10:15:32", null, "inbound", "call-attempt");

        when(missedCallRepository.existsByCallSid("CA_TEST_001")).thenReturn(false);
        when(callerService.resolveCallerName("+919876543210")).thenReturn("Rahul Sharma");
        when(missedCallRepository.save(any(MissedCall.class))).thenAnswer(invocation -> {
            MissedCall mc = invocation.getArgument(0);
            mc.setId(1L);
            return mc;
        });

        MissedCallResponse response = missedCallService.processWebhookEvent(request);

        assertEquals(1L, response.getId());
        assertEquals("+919876543210", response.getCallerNumber());
        assertEquals("Rahul Sharma", response.getCallerName());
        assertEquals("+912233445566", response.getDestinationNumber());
        assertEquals(CallStatus.NO_ANSWER.getExotelValue(), response.getCallStatus());
        verify(missedCallRepository, times(1)).save(any(MissedCall.class));
    }

    @Test
    void processWebhookEvent_duplicateCallSid_throwsException() {
        ExotelWebhookRequest request = new ExotelWebhookRequest(
                "CA_DUPLICATE", "+919876543210", null, "busy",
                "2026-06-19 11:00:00", null, null, null);

        when(missedCallRepository.existsByCallSid("CA_DUPLICATE")).thenReturn(true);

        assertThrows(DuplicateCallSidException.class,
                () -> missedCallService.processWebhookEvent(request));

        verify(missedCallRepository, never()).save(any(MissedCall.class));
    }
}
