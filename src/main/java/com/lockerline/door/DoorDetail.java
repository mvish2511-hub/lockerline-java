package com.lockerline.door;

import com.fasterxml.jackson.annotation.JsonInclude;

/** GET /towers/{tower}/doors/{no}; a holding door also carries its parcel, the flat and the dwell. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DoorDetail(String no, SizeClass size, DoorState state, String parcelId, String flat,
                         Long dwellSeconds) {
}
