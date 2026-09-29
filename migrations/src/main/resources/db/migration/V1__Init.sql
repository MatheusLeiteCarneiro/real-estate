CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_property
(
    id
    UUID
    NOT
    NULL
    DEFAULT
    gen_random_uuid
(
),
    title VARCHAR
(
    255
) NOT NULL,
    description TEXT NOT NULL,
    price NUMERIC
(
    15,
    2
) NOT NULL,
    type VARCHAR
(
    20
) NOT NULL,
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'AVAILABLE',
    suites INTEGER,
    bedrooms INTEGER,
    bathrooms INTEGER,
    area DOUBLE PRECISION,
    parking_spots INTEGER,
    street VARCHAR
(
    255
),
    number VARCHAR
(
    20
),
    complement VARCHAR
(
    255
),
    neighborhood VARCHAR
(
    255
),
    city VARCHAR
(
    255
),
    state VARCHAR
(
    2
),
    zip_code VARCHAR
(
    8
),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
                         WITH TIME ZONE,
                             CONSTRAINT pk_property PRIMARY KEY (id)
    );



CREATE TABLE IF NOT EXISTS tb_image
(
    id
    UUID
    NOT
    NULL
    DEFAULT
    gen_random_uuid
(
),
    url TEXT NOT NULL,
    property_id UUID NOT NULL,
    CONSTRAINT pk_image PRIMARY KEY
(
    id
),
    CONSTRAINT fk_image_property FOREIGN KEY
(
    property_id
) REFERENCES tb_property
(
    id
) ON DELETE CASCADE
    );