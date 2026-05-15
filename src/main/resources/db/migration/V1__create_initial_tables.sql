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
INSERT INTO users (username)
VALUES ('danil_ivanov');