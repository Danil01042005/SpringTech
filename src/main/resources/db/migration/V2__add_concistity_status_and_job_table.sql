ALTER TABLE persons ADD COLUMN IF NOT EXISTS policy_status varchar(10);

CREATE TABLE IF NOT EXISTS jobs(
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_id uuid NOT NULL,
    payload jsonb NOT NULL,
    entity_name TEXT NOT NULL,
    action_name TEXT NOT NULL,
    job_status TEXT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
)