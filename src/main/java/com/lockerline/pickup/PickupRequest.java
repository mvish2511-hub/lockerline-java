package com.lockerline.pickup;

import jakarta.validation.constraints.NotBlank;

/** POST /pickups: the code the resident keys in. */
public record PickupRequest(@NotBlank String code) {
}