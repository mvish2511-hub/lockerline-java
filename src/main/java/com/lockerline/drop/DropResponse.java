package com.lockerline.drop;

import java.time.Instant;

import com.lockerline.door.SizeClass;

/**
 * The answer to a drop: the parcel, the door that was chosen for it, and the pickup code - shown here and nowhere
 * else - with the moment it expires.
 */
public record DropResponse(String parcelId, String door, SizeClass sizeClass, String flat, Instant droppedAt,
                           String code, Instant expiresAt) {
}
