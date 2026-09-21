CREATE TABLE IF NOT EXISTS tb_user
(
    id
    UUID
    NOT
    NULL
    DEFAULT
    gen_random_uuid
(
),
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY
(
    id
)
    );

CREATE TABLE IF NOT EXISTS tb_user_role
(
    user_id
    UUID
    NOT
    NULL,
    authority
    TEXT
    NOT
    NULL,

    CONSTRAINT
    pk_user_role
    PRIMARY
    KEY
(
    user_id,
    authority
),
    CONSTRAINT fk_user_id FOREIGN KEY
(
    user_id
) REFERENCES tb_user
(
    id
)
    );