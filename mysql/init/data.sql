-- Sample data for testing (optional)
USE payment_db;

-- Insert some sample payments
INSERT INTO payments (id, amount, currency, status, created_at, updated_at, description, customer_id, merchant_id) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 100.50, 'USD', 'COMPLETED', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 'Test payment 1', 'CUST001', 'MERCH001'),
    ('550e8400-e29b-41d4-a716-446655440002', 250.00, 'EUR', 'COMPLETED', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 'Test payment 2', 'CUST002', 'MERCH001'),
    ('550e8400-e29b-41d4-a716-446655440003', 75.25, 'USD', 'PENDING', NOW() - INTERVAL 25 HOUR, NOW() - INTERVAL 25 HOUR, 'Test payment 3 - should be expired', 'CUST001', 'MERCH002'),
    ('550e8400-e29b-41d4-a716-446655440004', 500.00, 'GBP', 'PENDING', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR, 'Test payment 4', 'CUST003', 'MERCH002'),
    ('550e8400-e29b-41d4-a716-446655440005', 150.75, 'USD', 'FAILED', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 'Test payment 5', 'CUST002', 'MERCH001');
