package com.lockerline.parcel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.lockerline.error.NoSuchParcel;
import com.lockerline.error.ProblemExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/** One parcel. */
@RestController
@Tag(name = "parcels", description = "One parcel and its history")
public class ParcelController {

    private final ParcelService parcels;

    public ParcelController(ParcelService parcels) {
        this.parcels = parcels;
    }

    @GetMapping("/parcels/{parcelId}")
    @Operation(
            summary = "One parcel, with every event of its life in order")
    @ApiResponse(
            responseCode = "200",
            description = "The parcel")
    @ApiResponse(
            responseCode = "404",
            description = "No such parcel",
            content = @Content(
                    mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(
                            name = "no-such-parcel",
                            value = ProblemExamples.NO_SUCH_PARCEL)))
    public ParcelView parcel(@PathVariable String parcelId) {
        long id = ParcelIds.parse(parcelId)
                .orElseThrow(() -> new NoSuchParcel(parcelId));

        return parcels.parcel(id);
    }
}
