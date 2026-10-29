package com.lockerline.pickup;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/** A resident keys in a code. There is one tower, A. */
@RestController
public class PickupController {

    static final String TOWER = "A";

    private final PickupService pickups;

    public PickupController(PickupService pickups) {
        this.pickups = pickups;
    }

    @PostMapping("/pickups")
    public void pickup(@Valid @RequestBody PickupRequest body) {
        pickups.pickup(TOWER, body.code());
    }
}
