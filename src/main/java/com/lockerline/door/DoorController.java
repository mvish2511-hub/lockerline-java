package com.lockerline.door;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The wall. Parses the request and delegates; no rules live here. */
@RestController
@RequestMapping("/towers/{tower}/doors")
public class DoorController {

    private final DoorService doors;

    public DoorController(DoorService doors) {
        this.doors = doors;
    }

    @GetMapping
    public WallView wall(@PathVariable String tower,
            @RequestParam(required = false) SizeClass size,
            @RequestParam(required = false) DoorState state) {
        return doors.wall(tower, size, state);
    }

    @GetMapping("/{no}")
    public DoorDetail door(@PathVariable String tower, @PathVariable String no) {
        return doors.door(tower, no);
    }
}
