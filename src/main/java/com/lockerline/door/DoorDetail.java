package com.lockerline.door;

/** GET /towers/{tower}/doors/{no}. */
public record DoorDetail(String no, SizeClass size, DoorState state) {
}
