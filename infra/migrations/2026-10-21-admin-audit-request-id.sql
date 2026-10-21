-- ADR-085: every audited operator action keeps the request it was made in (X-Request-Id). Applied once to an existing
-- identity database (krizaka_users_db), as its owner role. Idempotent. New installs get the column from 10-identity.sql.
ALTER TABLE admin_audit ADD COLUMN IF NOT EXISTS request_id VARCHAR(64);
-- ADR-085: refused and failed actions are audited too (`ok` | `refused` | `failed`); existing rows are `ok`.
ALTER TABLE admin_audit ADD COLUMN IF NOT EXISTS outcome VARCHAR(12) NOT NULL DEFAULT 'ok';
