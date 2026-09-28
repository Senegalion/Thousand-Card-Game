# Persistence Additions (Design)

The baseline is `.prodready/define/data-model/schema.sql`. Design adds the tables below, and all of them become Flyway migrations in Scaffold/Implement.

## match_state_snapshot (ADR-004)

```sql
CREATE TABLE match_state_snapshot (
    match_id        UUID        PRIMARY KEY REFERENCES match (id) ON DELETE CASCADE,
    version         BIGINT      NOT NULL CHECK (version >= 0),
    schema_version  INTEGER     NOT NULL,
    state           JSONB       NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

- Written in the same transaction as any `match` or `match_participant` change, using `UPDATE … WHERE match_id = ? AND version = ?` for optimistic concurrency.
- Deleted when the match ends. The JSONB holds user ids of seated players, and account deletion is blocked while seated (A-14), so no deleted-user data can remain in snapshots.

## Spring Session JDBC (ADR-003)

The official `schema-postgresql.sql` from Spring Session, copied verbatim into a Flyway migration so that the schema is versioned and not auto-initialized:
- `spring_session` (with the indexed column `principal_name`, used to delete all of a user's sessions)
- `spring_session_attributes`

## Retention
| Data | Retention |
|---|---|
| `password_reset_token` | Deleted by a daily job once `expires_at` is more than 24 h in the past |
| Closed `game_table` rows | Kept only while referenced by a match. Unstarted tables are deleted when closed (A-07). |
| `spring_session` | Expired sessions are cleaned by Spring Session's scheduled cleanup |
| Logs (Loki) | 14 days. Logs contain no passwords, tokens or email addresses; user ids only. |
