package com.lockerline.drop;

import java.time.Instant;

import com.lockerline.door.SizeClass;

/** The answer to a drop: the parcel and the door that was chosen for it. */
public record DropResponse(String parcelId, String door, SizeClass sizeClass, String flat, Instant droppedAt) {
}
