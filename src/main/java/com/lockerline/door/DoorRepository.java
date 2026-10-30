package com.lockerline.door;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The doors table. */
@Repository
public class DoorRepository {

    /**
     * One door and, when it holds a parcel, that parcel's flat and drop time (null when the door is empty).
     */
    public record Row(long id, String no, SizeClass sizeClass, Long currentParcelId, String flat, Instant droppedAt) {
    }

    private static final String SELECT = """
            SELECT d.id, d.no, d.size_class, d.current_parcel_id, p.flat, p.dropped_at
            FROM doors d LEFT JOIN parcels p ON p.id = d.current_parcel_id
            """;

    private final JdbcClient jdbc;

    public DoorRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Every door of a tower, lowest number first. */
    public List<Row> all(long towerId) {
        return jdbc.sql(SELECT + "WHERE d.tower_id = ? ORDER BY d.no")
                .param(towerId).query(Row.class).list();
    }

    public Optional<Row> one(long towerId, String no) {
        return jdbc.sql(SELECT + "WHERE d.tower_id = ? AND d.no = ?")
                .params(towerId, no).query(Row.class).optional();
    }

    /** The free door of exactly this size with the lowest number, if any. */
    public Optional<Row> lowestFree(long towerId, SizeClass size) {
        return jdbc.sql(
                        SELECT + "WHERE d.tower_id = ? AND d.size_class = ? AND d.current_parcel_id IS NULL "
                                + "ORDER BY d.no LIMIT 1")
                .params(towerId, size.name()).query(Row.class).optional();
    }

    /** The sizes that still have at least one free door, in the order S, M, L. */
    public List<SizeClass> freeSizes(long towerId) {
        return jdbc.sql(
                        "SELECT DISTINCT size_class FROM doors WHERE tower_id = ? AND current_parcel_id IS NULL")
                .param(towerId).query(SizeClass.class).list().stream().sorted().toList();
    }

    /** Puts a parcel in a door. */
    public void hold(long doorId, long parcelId) {
        jdbc.sql("UPDATE doors SET current_parcel_id = ? WHERE id = ?")
                .params(parcelId, doorId).update();
    }

    /** Empties a door. */
    public void free(long doorId) {
        jdbc.sql("UPDATE doors SET current_parcel_id = NULL WHERE id = ?")
                .param(doorId)
                .update();
    }

    public void insert(long towerId, String no, SizeClass sizeClass) {
        jdbc.sql("INSERT INTO doors (tower_id, no, size_class) VALUES (?, ?, ?)")
                .params(towerId, no, sizeClass.name()).update();
    }
}
