UPDATE tb_property
SET broker_id = (SELECT u.id
                 FROM tb_user u
                          JOIN tb_user_role r ON r.user_id = u.id
                 WHERE r.authority = 'ROLE_ADMIN'
    LIMIT 1
    )
WHERE broker_id IS NULL;

ALTER TABLE tb_property
    ALTER COLUMN broker_id SET NOT NULL;

CREATE INDEX idx_property_broker_id ON tb_property (broker_id);