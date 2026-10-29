package com.lockerline.door;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The towers table. */
@Repository
public class TowerRepository {

    private final JdbcClient jdbc;

    public TowerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** The database id of the tower with this code. */
    public Optional<Long> idOf(String code) {
        return jdbc.sql("SELECT id FROM towers WHERE code = ?").param(code).query(Long.class).optional();
    }

    public long insert(String code, String name) {
        jdbc.sql("INSERT INTO towers (code, name) VALUES (?, ?)").params(code, name).update();
        return idOf(code).orElseThrow();
    }
}
