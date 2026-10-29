package com.lockerline.parcel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** One parcel. */
@RestController
public class ParcelController {

    private final ParcelService parcels;

    public ParcelController(ParcelService parcels) {
        this.parcels = parcels;
    }

    @GetMapping("/parcels/{parcelId}")
    public ParcelView parcel(@PathVariable String parcelId) {
        return parcels.parcel(parcelId);
    }
}
