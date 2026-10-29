package com.lockerline.drop;

import com.lockerline.door.SizeClass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** POST /drops: the parcel's size class and the flat it is for. */
public record DropRequest(@NotNull SizeClass sizeClass, @NotBlank String flat) {
}
