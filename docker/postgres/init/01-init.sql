-- Create additional databases for future microservices
CREATE DATABASE banking_accounts;
CREATE DATABASE banking_transactions;
CREATE DATABASE banking_notifications;
CREATE DATABASE banking_loans;

-- Create a monitoring user
CREATE USER monitoring_user WITH PASSWORD 'monitoring_pass_123';
GRANT CONNECT ON DATABASE banking_auth TO monitoring_user;
GRANT CONNECT ON DATABASE banking_accounts TO monitoring_user;
GRANT CONNECT ON DATABASE banking_transactions TO monitoring_user;
GRANT CONNECT ON DATABASE banking_notifications TO monitoring_user;
GRANT CONNECT ON DATABASE banking_loans TO monitoring_user;
