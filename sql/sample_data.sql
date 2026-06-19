-- ============================================================
-- Sample missed_calls data for local testing of the dashboard/APIs
-- ============================================================
USE exotel_missed_calls;

INSERT INTO missed_calls (caller_number, caller_name, destination_number, call_sid, call_status, missed_call_time) VALUES
('+919876543210', 'Rahul Sharma', '+918047123456', 'CAxxxxxxxxxxxxxxxx001', 'NO_ANSWER', NOW() - INTERVAL 1 HOUR),
('+919876543210', 'Rahul Sharma', '+918047123456', 'CAxxxxxxxxxxxxxxxx005', 'NO_ANSWER', NOW() - INTERVAL 2 HOUR),
('+919876543210', 'Rahul Sharma', '+918047199999', 'CAxxxxxxxxxxxxxxxx006', 'NO_ANSWER', NOW() - INTERVAL 3 HOUR),
('+919812345678', 'Priya Verma',  '+918047123456', 'CAxxxxxxxxxxxxxxxx002', 'BUSY',      NOW() - INTERVAL 4 HOUR),
('+919900998877', 'Unknown',      '+918047199999', 'CAxxxxxxxxxxxxxxxx003', 'FAILED',    NOW() - INTERVAL 1 DAY),
('+919900112233', 'Amit Kumar',   '+918047123456', 'CAxxxxxxxxxxxxxxxx004', 'CANCELED',  NOW() - INTERVAL 2 DAY)
ON DUPLICATE KEY UPDATE call_status = VALUES(call_status);
