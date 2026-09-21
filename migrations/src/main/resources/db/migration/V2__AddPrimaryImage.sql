ALTER TABLE tb_image
    ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX uq_primary_image_per_property ON tb_image (property_id) WHERE is_primary = true;