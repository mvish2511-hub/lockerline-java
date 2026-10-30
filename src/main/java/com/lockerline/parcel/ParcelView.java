package com.lockerline.parcel;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** GET /parcels/{parcelId}: the parcel and its history - the events, oldest first. */
@JsonPropertyOrder({"parcelId", "door", "sizeClass", "flat", "state", "droppedAt", "events"})
public record ParcelView(
        String parcelId,
        String door,
        String sizeClass,
        String flat,
        String state,
        Instant droppedAt,
        List<EventView> events) {
}