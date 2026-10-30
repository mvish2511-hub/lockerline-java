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

    /** Whether a parcel still waiting in its door already has this code hash. */
    public boolean codeInUse(String codeHash) {
        return jdbc.sql("SELECT count(*) FROM parcels WHERE code_hash = ? AND code_used_at IS NULL")
                .param(codeHash)
                .query(Long.class).single() > 0;
    }

    /**
     * A parcel as a pickup sees it: its code's stored hash and expiry, whether the code was used,
     * when it was dropped and the door it is in.
     */
    public record CodeRow(long id, String codeHash, Instant codeExpiresAt, Instant codeUsedAt, Instant droppedAt,
                          long doorId, String door) {
    }

    /** The newest parcel of this tower whose code has this hash, if any. */
    public Optional<CodeRow> findByCodeHash(long towerId, String codeHash) {
        return jdbc.sql(
                        """
                        SELECT p.id, p.code_hash, p.code_expires_at, p.code_used_at, p.dropped_at, p.door_id,
                               d.no AS door
                        FROM parcels p JOIN doors d ON d.id = p.door_id
                        WHERE p.tower_id = ? AND p.code_hash = ? ORDER BY p.id DESC LIMIT 1""")
                .params(towerId, codeHash)
                .query(CodeRow.class)
                .optional();
    }

    /** The parcel is collected: its code used, and how long it waited stored. */
    public void collect(long id, Instant at, long dwellSeconds) {
        jdbc.sql("UPDATE parcels SET state = 'collected', collected_at = ?, code_used_at = ?, dwell_seconds = ? "
                        + "WHERE id = ?")
                .params(at, at, dwellSeconds, id)
                .update();
    }

    public Optional<Row> find(long id) {
        return jdbc.sql("""
                        SELECT p.id, d.no AS door, p.size_class, p.flat, p.state, p.dropped_at
                        FROM parcels p JOIN doors d ON d.id = p.door_id WHERE p.id = ?""")
                .param(id)
                .query(Row.class)
                .optional();
    }
}
