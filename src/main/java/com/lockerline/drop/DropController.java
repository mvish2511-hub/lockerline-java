package com.lockerline.drop;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/** A courier books a drop. There is one tower, A. */
@RestController
public class DropController {

    static final String TOWER = "A";

    private final DropService drops;

    public DropController(DropService drops) {
        this.drops = drops;
    }

    @PostMapping("/drops")
    public ResponseEntity<DropResponse> drop(@Valid @RequestBody DropRequest body) {
        DropResponse made = drops.drop(TOWER, body.sizeClass(), body.flat());
        return ResponseEntity.created(URI.create("/parcels/" + made.parcelId())).body(made);
    }
}
