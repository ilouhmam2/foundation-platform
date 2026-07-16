CREATE TABLE test_items
(
    id         BIGSERIAL    NOT NULL PRIMARY KEY,
    label      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);
