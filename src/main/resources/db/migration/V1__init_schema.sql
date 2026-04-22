CREATE TABLE users (
    id UUID PRIMARY KEY,
    external_id VARCHAR(64) NOT NULL UNIQUE,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE cars (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    vin VARCHAR(64) NOT NULL,
    brand VARCHAR(128) NOT NULL,
    model VARCHAR(128) NOT NULL,
    production_year INT NOT NULL,
    CONSTRAINT fk_cars_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_vin UNIQUE (user_id, vin)
);
