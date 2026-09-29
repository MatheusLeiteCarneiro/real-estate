-- Sample data for local development: a broker user and 6 properties (5 available,
-- 1 not) with 1 image each.
--
-- RUN COMMAND:
-- psql "$DATABASE_URL" -f migrations/seed/seed.sql
--
-- Run only once. Running it again will fail on the unique username
-- constraint; if you want to start over, delete the seeded rows first:
--
--  DELETE FROM tb_property WHERE broker_id = (SELECT id FROM tb_user WHERE username = 'broker');
--  DELETE FROM tb_user WHERE username = 'broker';

BEGIN;

-- broker / Broker123!
INSERT INTO tb_user (id, username, password, active, created_at, updated_at)
VALUES ('42ab6f2a-0f8e-4f0d-a4c1-8a2457af9164', 'broker',
        '$2y$10$3zDLt/c6soH1//kDxChVsOiEMOYkjVU8YRyKYGQ5AOjTrnU/1CoYa', true, NOW(), NOW());

INSERT INTO tb_user_role (user_id, authority)
VALUES ('42ab6f2a-0f8e-4f0d-a4c1-8a2457af9164', 'ROLE_BROKER');

INSERT INTO tb_property (id, title, description, price, transaction_type, category, suites,
                          bedrooms, bathrooms, area, parking_spots, street, number, complement,
                          neighborhood, city, state, zip_code, broker_id, available, created_at,
                          updated_at)
VALUES ('b0c8abba-7a7a-465b-a050-0961026ac7fa', '2-bedroom apartment with park view',
        'Renovated apartment close to the subway, morning sun.', 650000.00, 'SALE', 'APARTMENT',
        1, 2, 2, 72.50, 1, 'Rua dos Pinheiros', '450', NULL, 'Pinheiros', 'São Paulo', 'SP',
        '05422000', '42ab6f2a-0f8e-4f0d-a4c1-8a2457af9164', true, NOW(), NOW()),

       ('97bbce0c-2268-43a7-ba14-b93d767b2b59', 'House in a gated community',
        'Single-story house, large backyard, full leisure area in the condominium.', 1200000.00,
        'SALE', 'HOUSE', 1, 3, 3, 180.00, 2, 'Alameda das Palmeiras', '120', 'Casa 14',
        'Alphaville', 'Barueri', 'SP', '06454000', '42ab6f2a-0f8e-4f0d-a4c1-8a2457af9164', true,
        NOW(), NOW()),

       ('f8a4d458-0d1d-45ef-b1c9-ed5cb001d441', 'Furnished studio in Vila Madalena',
        'Compact furnished studio, ideal for people working in the area.', 2800.00, 'RENT',
        'STUDIO', 0, 1, 1, 28.00, 0, 'Rua Harmonia', '210', 'Apto 51', 'Vila Madalena',
        'São Paulo', 'SP', '05435000', '42ab6f2a-0f8e-4f0d-a4c1-8a2457af9164', true, NOW(), NOW()),

       ('e83a5aa8-f4b6-44ce-afe9-b47c8b534a5e', 'Commercial office downtown',
        'Move-in ready office, building with 24h reception and parking.', 3500.00, 'RENT',
        'COMMERCIAL', NULL, NULL, 1, 45.00, 1, 'Avenida Francisco Glicério', '800', 'Sala 302',
        'Centro', 'Campinas', 'SP', '13012100', '42ab6f2a-0f8e-4f0d-a4c1-8a2457af9164', true,
        NOW(), NOW()),

       ('6d19350e-b27c-4c17-9948-1734fbb1e7f3', 'Flat lot in a gated community',
        'Ready-to-build lot, paperwork in order.', 380000.00, 'SALE', 'LAND', NULL, NULL, NULL,
        500.00, NULL, 'Estrada Municipal', 's/n', 'Lote 22', 'Zona Rural', 'Itu', 'SP',
        '13300000', '42ab6f2a-0f8e-4f0d-a4c1-8a2457af9164', true, NOW(), NOW()),

       ('1610a5ce-a701-4f64-b5c8-76c83dc3be07', 'Farm with a natural spring',
        'Farm with main house, orchard, and its own water spring.', 950000.00, 'SALE', 'FARM', 1,
        4, 2, 15000.00, 4, 'Estrada da Serra', 's/n', NULL, 'Zona Rural', 'Ibiúna', 'SP',
        '18150000', '42ab6f2a-0f8e-4f0d-a4c1-8a2457af9164', false, NOW(), NOW());

INSERT INTO tb_image (property_id, url, file_identifier, is_primary)
VALUES ('b0c8abba-7a7a-465b-a050-0961026ac7fa',
        'https://picsum.photos/seed/b0c8abba-7a7a-465b-a050-0961026ac7fa/800/600',
        'seed/b0c8abba-7a7a-465b-a050-0961026ac7fa', true),
       ('97bbce0c-2268-43a7-ba14-b93d767b2b59',
        'https://picsum.photos/seed/97bbce0c-2268-43a7-ba14-b93d767b2b59/800/600',
        'seed/97bbce0c-2268-43a7-ba14-b93d767b2b59', true),
       ('f8a4d458-0d1d-45ef-b1c9-ed5cb001d441',
        'https://picsum.photos/seed/f8a4d458-0d1d-45ef-b1c9-ed5cb001d441/800/600',
        'seed/f8a4d458-0d1d-45ef-b1c9-ed5cb001d441', true),
       ('e83a5aa8-f4b6-44ce-afe9-b47c8b534a5e',
        'https://picsum.photos/seed/e83a5aa8-f4b6-44ce-afe9-b47c8b534a5e/800/600',
        'seed/e83a5aa8-f4b6-44ce-afe9-b47c8b534a5e', true),
       ('6d19350e-b27c-4c17-9948-1734fbb1e7f3',
        'https://picsum.photos/seed/6d19350e-b27c-4c17-9948-1734fbb1e7f3/800/600',
        'seed/6d19350e-b27c-4c17-9948-1734fbb1e7f3', true),
       ('1610a5ce-a701-4f64-b5c8-76c83dc3be07',
        'https://picsum.photos/seed/1610a5ce-a701-4f64-b5c8-76c83dc3be07/800/600',
        'seed/1610a5ce-a701-4f64-b5c8-76c83dc3be07', true);

COMMIT;
