-- The tower, its doors and its residents. Run at every start-up, so every table is created only if missing.
-- A door's state is not stored: it is free when current_parcel_id is null and holding otherwise.

CREATE TABLE IF NOT EXISTS towers (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(8)  NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL
);

CREATE TABLE IF NOT EXISTS doors (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    tower_id          BIGINT     NOT NULL REFERENCES towers (id),
    no                VARCHAR(8) NOT NULL,
    size_class        VARCHAR(1) NOT NULL,
    current_parcel_id BIGINT,
    UNIQUE (tower_id, no)
);

CREATE TABLE IF NOT EXISTS residents (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    tower_id BIGINT      NOT NULL REFERENCES towers (id),
    flat     VARCHAR(8)  NOT NULL,
    name     VARCHAR(80) NOT NULL,
    UNIQUE (tower_id, flat)
);
