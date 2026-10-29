package com.lockerline.door;

import com.fasterxml.jackson.annotation.JsonInclude;

/** One door on the wall; a holding door also carries its parcel and how long it has been there. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WallDoor(String no, SizeClass size, DoorState state, String parcelId, Long dwellSeconds) {
}
