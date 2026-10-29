package com.lockerline.door;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The residents table. */
@Repository
public class ResidentRepository {

    private final JdbcClient jdbc;

    public ResidentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** True when a resident lives in this flat of this tower. */
    public boolean exists(long towerId, String flat) {
        return jdbc.sql("SELECT count(*) FROM residents WHERE tower_id = ? AND flat = ?")
                .params(towerId, flat)
                .query(Long.class).single() > 0;
    }

    public void insert(long towerId, String flat, String name) {
        jdbc.sql("INSERT INTO residents (tower_id, flat, name) VALUES (?, ?, ?)")
                .params(towerId, flat, name).update();
    }
}
