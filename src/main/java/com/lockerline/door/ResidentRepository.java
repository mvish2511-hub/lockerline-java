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

    public void insert(long towerId, String flat, String name) {
        jdbc.sql("INSERT INTO residents (tower_id, flat, name) VALUES (?, ?, ?)").params(towerId, flat, name).update();
    }
}
