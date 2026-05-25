CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE IF NOT EXISTS users(
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    username TEXT
);