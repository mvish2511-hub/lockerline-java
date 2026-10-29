package com.lockerline.door;

import java.util.List;

/**
 * GET /towers/{tower}/doors.
 *
 * @param free how many of the doors shown are free
 */
public record WallView(String tower, int free, List<WallDoor> doors) {
}
