package com.lockerline.parcel;

import java.time.Instant;

import com.lockerline.door.SizeClass;

/** GET /parcels/{parcelId}. */
public record ParcelView(String parcelId, String door, SizeClass sizeClass, String flat, String state,
                         Instant droppedAt) {
}
