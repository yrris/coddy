-- V3: Gallery features — public apps, likes, featured mapping
-- Note: is_featured column already exists from V1 but was unmapped in entity

ALTER TABLE app_project ADD COLUMN IF NOT EXISTS is_public BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE app_project ADD COLUMN IF NOT EXISTS like_count INTEGER NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_app_project_public ON app_project(is_public) WHERE is_public = true AND is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_app_project_featured ON app_project(is_featured) WHERE is_featured = true AND is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_app_project_like_count ON app_project(like_count DESC);

CREATE TABLE IF NOT EXISTS app_like (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    app_id BIGINT NOT NULL REFERENCES app_project(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_app_like_unique ON app_like(user_id, app_id);
CREATE INDEX IF NOT EXISTS idx_app_like_app ON app_like(app_id);
