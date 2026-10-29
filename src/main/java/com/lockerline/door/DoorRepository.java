package com.lockerline.door;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The doors table. */
@Repository
public class DoorRepository {

    /** One doors row: its number, its size, and the parcel inside it (null when empty). */
    public record Row(String no, SizeClass sizeClass, Long currentParcelId) {
    }

    private final JdbcClient jdbc;

    public DoorRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Every door of a tower, lowest number first. */
    public List<Row> all(long towerId) {
        return jdbc.sql("SELECT no, size_class, current_parcel_id FROM doors WHERE tower_id = ? ORDER BY no")
                .param(towerId).query(Row.class).list();
    }

    public Optional<Row> one(long towerId, String no) {
        return jdbc.sql("SELECT no, size_class, current_parcel_id FROM doors WHERE tower_id = ? AND no = ?")
                .params(towerId, no).query(Row.class).optional();
    }

    public void insert(long towerId, String no, SizeClass sizeClass) {
        jdbc.sql("INSERT INTO doors (tower_id, no, size_class) VALUES (?, ?, ?)")
                .params(towerId, no, sizeClass.name()).update();
    }
}
