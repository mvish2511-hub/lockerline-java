package com.lockerline.config;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lockerline.door.SizeClass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/** The live settings, read-only - the rule book screen reads the free window from here.*/
@RestController
@Tag(name = "settings", description = "The rules the app is running with")
public class SettingsController {

    private final LockerLineSettings settings;

    public SettingsController(LockerLineSettings settings) {
        this.settings = settings;
    }

    @GetMapping("/settings")
    @Operation(summary = "The live free window and the door sizes")
    @ApiResponse(responseCode = "200", description = "The current settings")
    public SettingsView settings() {
        return new SettingsView(settings.freeWindowHours(), List.of(SizeClass.values()));
    }
}
