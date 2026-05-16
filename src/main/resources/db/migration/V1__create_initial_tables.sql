CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE IF NOT EXISTS users(
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    username TEXT
);
CREATE TABLE IF NOT EXISTS orders(
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id uuid REFERENCES users(id) ON DELETE SET NULL,
    order_details TEXT
);
CREATE TABLE IF NOT EXISTS person(
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name TEXT,
    age integer CHECK ( age >= 16 )
);
CREATE TABLE IF NOT EXISTS passport(
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    passport_number TEXT NOT NULL CHECK (length(passport_number) = 6),
    person_id uuid UNIQUE REFERENCES person(id) ON DELETE CASCADE
)