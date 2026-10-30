package com.lockerline.pickup;

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
@Tag(name = "pickups", description = "A resident opens their door with a code")
public class PickupController {

    private static final String TOWER = "A";

    private final PickupService pickups;

    public PickupController(PickupService pickups) {
        this.pickups = pickups;
    }

    @PostMapping("/pickups")
    @Operation(
            summary = "Open the door a code belongs to: once, inside the free window")
    @ApiResponse(
            responseCode = "200",
            description = "The door opened; how long the parcel waited")
    @ApiResponse(
            responseCode = "404",
            description = "No such code, or its window has passed",
            content = @Content(
                    mediaType = ProblemExamples.PROBLEM,
                    examples = {
                            @ExampleObject(
                                    name = "no-such-code",
                                    value = ProblemExamples.NO_SUCH_CODE),
                            @ExampleObject(
                                    name = "code-expired",
                                    value = ProblemExamples.CODE_EXPIRED)}))
    @ApiResponse(
            responseCode = "409",
            description = "The code has already opened its door",
            content = @Content(
                    mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(
                            name = "code-used",
                            value = ProblemExamples.CODE_USED)))
    public ResponseEntity<PickupResponse> pickup(
            @Valid @RequestBody PickupRequest body) {
        return ResponseEntity.ok(pickups.pickup(TOWER, body.code()));
    }
}
