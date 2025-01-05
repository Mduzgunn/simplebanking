-- Insert sample accounts
INSERT INTO account (owner, account_number, balance, create_date, last_transaction_approval_code)
VALUES
    ('Kerem Karaca', '669-7788', 100.0, NOW(), NULL),
    ('Demet Demircan', '669-7789', 2000.0, NOW(), NULL),
    ('Cemil Cemil', '669-7790', 5000.0, NOW(), NULL);

-- Insert sample transactions
INSERT INTO transaction (date, amount, approval_code, account_id, type)
SELECT NOW(), 1000.0, 'd46a3318-1d43-4826-8ea6-8d1f11d11111', id, 'DepositTransaction'
FROM account WHERE account_number = '669-7788';

INSERT INTO transaction (date, amount, approval_code, account_id, type)
SELECT NOW(), 50.0, 'd46a3318-1d43-4826-8ea6-8d1f11d22222', id, 'WithdrawalTransaction'
FROM account WHERE account_number = '669-7788';

INSERT INTO transaction (date, amount, approval_code, account_id, type)
SELECT NOW(), 100.0, 'd46a3318-1d43-4826-8ea6-8d1f11d33333', id, 'DepositTransaction'
FROM account WHERE account_number = '669-7789';

INSERT INTO transaction (date, amount, approval_code, account_id, type)
SELECT NOW(), 95.0, 'd46a3318-1d43-4826-8ea6-8d1f11d55555', id, 'PhoneBillPaymentTransaction'
FROM account WHERE account_number = '669-7790';

-- Update account balances and last transaction approval codes
UPDATE account
SET balance = 
    CASE 
        WHEN account_number = '669-7788' THEN 1050.0  -- 100 + 1000 - 50
        WHEN account_number = '669-7789' THEN 2100.0  -- 2000 + 100
        WHEN account_number = '669-7790' THEN 4905.0  -- 5000 - 95
    END,
    last_transaction_approval_code = 
    CASE 
        WHEN account_number = '669-7788' THEN 'd46a3318-1d43-4826-8ea6-8d1f11d22222'
        WHEN account_number = '669-7789' THEN 'd46a3318-1d43-4826-8ea6-8d1f11d33333'
        WHEN account_number = '669-7790' THEN 'd46a3318-1d43-4826-8ea6-8d1f11d55555'
    END; 
