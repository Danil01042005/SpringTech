CREATE TABLE IF NOT EXISTS retryable_task(
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    payload jsonb,
    type varchar(15),
    status varchar(10),
    retry_time TIMESTAMP WITH TIME ZONE,
    attempts INTEGER,
    lease_expires_at TIMESTAMP,
    lease_token UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
ALTER TABLE actors ADD COLUMN IF NOT EXISTS policy_status varchar(10);