ALTER TABLE tb_user
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP;

UPDATE tb_user
SET created_at = NOW(),
    updated_at = NOW()
WHERE created_at IS NULL;

ALTER TABLE tb_user
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;