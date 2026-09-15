ALTER TABLE tb_property
    ADD COLUMN broker_id UUID REFERENCES tb_user (id);