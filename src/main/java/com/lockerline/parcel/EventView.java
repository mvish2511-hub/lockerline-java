package com.lockerline.parcel;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * One line of a parcel's history: when, what, and that event's own facts (the door, the expiry, the dwell), which
 * appear next to `at` and `kind` in the JSON.
 */
@JsonPropertyOrder({"at", "kind"})
public record EventView(Instant at, String kind, @JsonAnyGetter Map<String, Object> facts) {
}
