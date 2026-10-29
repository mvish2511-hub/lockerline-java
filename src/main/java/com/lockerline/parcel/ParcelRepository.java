package com.lockerline.parcel;

import java.time.Instant;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.lockerline.door.SizeClass;

/** The parcels table. */
@Repository
public class ParcelRepository {

    /** One parcel, with the number of the door it went into. */
    public record Row(long id, String door, SizeClass sizeClass, String flat, String state, Instant droppedAt) {
    }

    private final JdbcClient jdbc;

    public ParcelRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** A new parcel, holding, in this door, with its code's hash and expiry; its database id. */
    public long insert(long towerId, String flat, SizeClass sizeClass, long doorId, Instant droppedAt,
                       String codeHash, Instant codeExpiresAt) {
        KeyHolder key = new GeneratedKeyHolder();

        jdbc.sql("INSERT INTO parcels (tower_id, flat, size_class, door_id, dropped_at, "
                        + "state, code_hash, code_expires_at) "
                        + "VALUES (?, ?, ?, ?, ?, 'holding', ?, ?)")
                .params(towerId, flat, sizeClass.name(), doorId, droppedAt, codeHash, codeExpiresAt)
                .update(key, "id");

        return key.getKeyAs(Long.class);
    }

    /** Whether a parcel already holds this code hash, unused (a new code must differ). */
    public boolean codeInUse(String codeHash) {
        return jdbc.sql("SELECT count(*) FROM parcels WHERE code_hash = ? AND code_used_at IS NULL")
                .param(codeHash)
                .query(Long.class).single() > 0;
    }

    /** A parcel as a pickup sees it: its code's stored hash and expiry, and code_used_at (nothing sets it yet). */
    public record CodeRow(long id, String codeHash, Instant codeExpiresAt, Instant codeUsedAt) {
    }

    /** The parcel of this tower whose code has this hash, if any (the newest, should two ever share it). */
    public Optional<CodeRow> findByCodeHash(long towerId, String codeHash) {
        return jdbc.sql("SELECT id, code_hash, code_expires_at, code_used_at FROM parcels "
                        + "WHERE tower_id = ? AND code_hash = ? ORDER BY id DESC LIMIT 1")
                .params(towerId, codeHash)
                .query(CodeRow.class)
                .optional();
    }

    public Optional<Row> find(long id) {
        return jdbc.sql("""
                        SELECT p.id, d.no AS door, p.size_class, p.flat, p.state, p.dropped_at
                        FROM parcels p JOIN doors d ON d.id = p.door_id WHERE p.id = ?""")
                .param(id).query(Row.class).optional();
    }
}
