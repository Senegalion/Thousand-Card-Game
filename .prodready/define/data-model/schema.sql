-- Thousand Online: MVP relational schema (PostgreSQL)
-- Source of truth for the Define phase; implemented later as Flyway migrations and used by jOOQ codegen.
-- Live in-progress match state (hands, tricks, bids, timers) is NOT stored here (see entities.md, A-12).

CREATE TABLE app_user (
    id                      UUID         PRIMARY KEY,
    email                   VARCHAR(254) NOT NULL,
    username                VARCHAR(32)  NOT NULL,
    password_hash           VARCHAR(255) NOT NULL,
    credentials_changed_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_app_user_email_lower    ON app_user (lower(email));
CREATE UNIQUE INDEX ux_app_user_username_lower ON app_user (lower(username));

CREATE TABLE password_reset_token (
    id          UUID         PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    token_hash  VARCHAR(128) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_password_reset_token_user ON password_reset_token (user_id);

CREATE TABLE game_table (
    id            UUID        PRIMARY KEY,
    join_code     VARCHAR(32) NOT NULL UNIQUE,
    player_count  SMALLINT    NOT NULL CHECK (player_count BETWEEN 2 AND 4),
    host_user_id  UUID        REFERENCES app_user (id) ON DELETE SET NULL,
    status        VARCHAR(16) NOT NULL CHECK (status IN ('OPEN', 'IN_MATCH', 'CLOSED')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    closed_at     TIMESTAMPTZ,
    CHECK ((status = 'CLOSED') = (closed_at IS NOT NULL))
);

CREATE TABLE table_seat (
    table_id    UUID        NOT NULL REFERENCES game_table (id) ON DELETE CASCADE,
    seat_index  SMALLINT    NOT NULL CHECK (seat_index BETWEEN 0 AND 3),
    user_id     UUID        NOT NULL UNIQUE REFERENCES app_user (id) ON DELETE RESTRICT,
    joined_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (table_id, seat_index)
);

CREATE TABLE match (
    id               UUID        PRIMARY KEY,
    table_id         UUID        REFERENCES game_table (id) ON DELETE SET NULL,
    player_count     SMALLINT    NOT NULL CHECK (player_count BETWEEN 2 AND 4),
    ruleset_version  VARCHAR(32) NOT NULL,
    status           VARCHAR(16) NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FORFEIT', 'ABANDONED')),
    started_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    ended_at         TIMESTAMPTZ,
    CHECK ((status = 'IN_PROGRESS') = (ended_at IS NULL))
);

CREATE INDEX ix_match_status ON match (status);

CREATE TABLE match_participant (
    id           UUID        PRIMARY KEY,
    match_id     UUID        NOT NULL REFERENCES match (id) ON DELETE CASCADE,
    seat_index   SMALLINT    NOT NULL CHECK (seat_index BETWEEN 0 AND 3),
    -- NULL after account deletion => rendered as "Deleted player"; no identity snapshot is stored.
    user_id      UUID        REFERENCES app_user (id) ON DELETE SET NULL,
    final_score  INTEGER,
    outcome      VARCHAR(16) CHECK (outcome IN ('WIN', 'LOSS', 'DRAW', 'FORFEITED', 'WIN_BY_FORFEIT', 'ABANDONED')),
    UNIQUE (match_id, seat_index)
);

CREATE INDEX ix_match_participant_user ON match_participant (user_id);
