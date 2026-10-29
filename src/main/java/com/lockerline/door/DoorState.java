package com.lockerline.door;

/**
 * A door's state, derived on every read from what the door holds - never stored. The constant names are the values on
 * the wire and in the {@code ?state=} filter, so they are lower case.
 */
public enum DoorState {
    free, holding
}
