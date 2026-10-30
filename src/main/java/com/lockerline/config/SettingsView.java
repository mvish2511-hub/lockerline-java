package com.lockerline.config;

import java.util.List;

import com.lockerline.door.SizeClass;

/** GET /settings: the rules the app is running with right now. */
public record SettingsView(int freeWindowHours, List<SizeClass> sizes) {
}
