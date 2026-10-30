package com.lockerline.door;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lockerline.error.ProblemExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/** The wall of doors, and one door. */
@RestController
@RequestMapping("/towers/{tower}/doors")
@Tag(name = "doors", description = "The wall of doors, and one door")
public class DoorController {

    private final DoorService doors;

    public DoorController(DoorService doors) {
        this.doors = doors;
    }

    @GetMapping
    @Operation(
            summary = "The wall: every door with its state, optionally filtered by size and state")
    @ApiResponse(
            responseCode = "200",
            description = "The doors shown and how many of them are free")
    @ApiResponse(
            responseCode = "400",
            description = "A size or state that is not allowed",
            content = @Content(
                    mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(
                            name = "validation",
                            value = ProblemExamples.VALIDATION_QUERY)))
    @ApiResponse(
            responseCode = "404",
            description = "No such tower",
            content = @Content(
                    mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(
                            name = "no-such-tower",
                            value = ProblemExamples.NO_SUCH_TOWER)))
    public WallView wall(
            @PathVariable String tower,
            @RequestParam(required = false) SizeClass size,
            @RequestParam(required = false) DoorState state) {
        return doors.wall(tower, size, state);
    }

    @GetMapping("/{no}")
    @Operation(
            summary = "One door: its size, its state and, when it holds a parcel, which one and for how long")
    @ApiResponse(
            responseCode = "200",
            description = "The door")
    @ApiResponse(
            responseCode = "404",
            description = "No such door, or no such tower",
            content = @Content(
                    mediaType = ProblemExamples.PROBLEM,
                    examples = {
                            @ExampleObject(
                                    name = "no-such-door",
                                    value = ProblemExamples.NO_SUCH_DOOR),
                            @ExampleObject(
                                    name = "no-such-tower",
                                    value = ProblemExamples.NO_SUCH_TOWER)}))
    public DoorDetail one(
            @PathVariable String tower,
            @PathVariable String no) {
        return doors.door(tower, no);
    }
}
