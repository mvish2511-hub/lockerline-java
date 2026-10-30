package com.lockerline.drop;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lockerline.error.ProblemExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "drops", description = "A courier books a door for a parcel")
public class DropController {

    private static final String TOWER = "A";

    private final DropService drops;

    public DropController(DropService drops) {
        this.drops = drops;
    }

    @PostMapping("/drops")
    @Operation(
            summary = "Book a door for a parcel: the lowest free door of its size, and a pickup code")
    @ApiResponse(
            responseCode = "201",
            description = "A door was chosen; the parcel is at the Location given")
    @ApiResponse(
            responseCode = "400",
            description = "No such flat, or a body that is not valid",
            content = @Content(
                    mediaType = ProblemExamples.PROBLEM,
                    examples = {
                            @ExampleObject(
                                    name = "no-such-flat",
                                    value = ProblemExamples.NO_SUCH_FLAT),
                            @ExampleObject(
                                    name = "validation",
                                    value = ProblemExamples.VALIDATION_DROP)}))
    @ApiResponse(
            responseCode = "409",
            description = "No free door of that size",
            content = @Content(
                    mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(
                            name = "no-door-free",
                            value = ProblemExamples.NO_DOOR_FREE)))
    public ResponseEntity<DropResponse> drop(@Valid @RequestBody DropRequest body) {
        DropResponse result = drops.drop(TOWER, body.sizeClass(), body.flat());

        return ResponseEntity
                .created(URI.create("/parcels/" + result.parcelId()))
                .body(result);
    }
}
