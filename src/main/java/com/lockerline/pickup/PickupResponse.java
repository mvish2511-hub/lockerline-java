package com.lockerline.pickup;

import java.time.Instant;

/** The answer to a pickup: the door that opened and how long the parcel waited in it. */
public record PickupResponse(String parcelId, String door, Instant openedAt, long dwellSeconds) {
}
