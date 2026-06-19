-- ============================================================
-- Sample missed_calls data for local testing of the dashboard/APIs
-- ============================================================
USE exotel_missed_calls;

INSERT INTO missed_calls (caller_number, caller_name, call_sid, call_status, missed_call_time) VALUES
('+919876543210', 'Rahul Sharma', 'CAxxxxxxxxxxxxxxxx001', 'NO_ANSWER', NOW() - INTERVAL 1 HOUR),
('+919812345678', 'Priya Verma',  'CAxxxxxxxxxxxxxxxx002', 'BUSY',      NOW() - INTERVAL 3 HOUR),
('+919900998877', 'Unknown',      'CAxxxxxxxxxxxxxxxx003', 'FAILED',    NOW() - INTERVAL 1 DAY),
('+919900112233', 'Amit Kumar',   'CAxxxxxxxxxxxxxxxx004', 'CANCELED',  NOW() - INTERVAL 2 DAY)
ON DUPLICATE KEY UPDATE call_status = VALUES(call_status);
