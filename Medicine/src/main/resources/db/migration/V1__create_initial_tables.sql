CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE IF NOT EXISTS medical_policies(
    person_id UUID PRIMARY KEY,
    policy_number TEXT NOT NULL CHECK(length(policy_number) = 6),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);