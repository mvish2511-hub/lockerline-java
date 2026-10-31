package com.lockerline.parcel;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The append-only log. It can add an event and read a parcel's events - nothing else, so nothing in the app can
 * change or remove one. Called inside the transaction of the change it records, so a rolled-back change leaves no
 * event behind.
   * Every event also leaves one log line on stdout - its kind, the parcel and its facts (never a code or a name).
 */
@Repository
public class EventLog {

    private record Row(Instant at, String kind, String data) {
    }

    private static final Logger log = LoggerFactory.getLogger("com.lockerline.events");
    private final JdbcClient jdbc;
    private final ObjectMapper json;

    public EventLog(JdbcClient jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public void append(long towerId, long parcelId, String kind, Instant at, Map<String, Object> facts) {
        jdbc.sql("INSERT INTO events (tower_id, parcel_id, kind, at, data) VALUES (?, ?, ?, ?, ?)")
                .params(towerId, parcelId, kind, at, write(facts)).update();
        log.info("event kind={} parcelId={} facts={}", kind, ParcelIds.format(parcelId), facts);
    }

    /** A parcel's events, oldest first. */
    public List<EventView> forParcel(long parcelId) {
        return jdbc.sql("SELECT at, kind, data FROM events WHERE parcel_id = ? ORDER BY at, id").param(parcelId)
                .query(Row.class).list().stream()
                .map(r -> new EventView(r.at(), r.kind(), read(r.data()))).toList();
    }

private String write(Map<String, Object> facts) {
        try {
            return json.writeValueAsString(facts);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("event facts must be JSON", e);
        }
    }

    private Map<String, Object> read(String data) {
        try {
            return json.readValue(data, new TypeReference<Map<String, Object>>() { });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("an events row holds data that is not JSON", e);
        }
    }
}