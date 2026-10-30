package com.lockerline.drop;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lockerline.config.LockerLineSettings;
import com.lockerline.door.DoorRepository;
import com.lockerline.door.DoorService;
import com.lockerline.door.SizeClass;
import com.lockerline.error.NoDoorFree;
import com.lockerline.parcel.EventLog;
import com.lockerline.parcel.ParcelIds;
import com.lockerline.parcel.ParcelRepository;
import com.lockerline.parcel.PickupCodes;

@Service
public class DropService {

    private final DoorService doorService;
    private final DoorRepository doors;
    private final ParcelRepository parcels;
    private final PickupCodes codes;
    private final EventLog events;
    private final LockerLineSettings settings;
    private final Clock clock;

    public DropService(
            DoorService doorService,
            DoorRepository doors,
            ParcelRepository parcels,
            PickupCodes codes,
            EventLog events,
            LockerLineSettings settings,
            Clock clock) {

        this.doorService = doorService;
        this.doors = doors;
        this.parcels = parcels;
        this.codes = codes;
        this.events = events;
        this.settings = settings;
        this.clock = clock;
    }

    @Transactional
    public DropResponse drop(String tower, SizeClass sizeClass, String flat) {

        long towerId = doorService.towerId(tower);

        var door = doors.lowestFree(towerId, sizeClass)
                .orElseThrow(() ->
                        new NoDoorFree(sizeClass, doors.freeSizes(towerId)));

        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);

        Instant expires = now.plus(
                settings.freeWindowHours(),
                ChronoUnit.HOURS);

        String code;
        String hash;

        do {
            code = codes.newCode();
            hash = codes.hash(code);
        } while (parcels.codeInUse(hash));

        long parcelId = parcels.insert(
                towerId,
                flat,
                sizeClass,
                door.id(),
                now,
                hash,
                expires);

        doors.hold(door.id(), parcelId);

        events.append(
                towerId,
                parcelId,
                "dropped",
                now,
                Map.of("door", door.no()));

        events.append(
                towerId,
                parcelId,
                "code_issued",
                now,
                Map.of("expiresAt", expires.toString()));

        return new DropResponse(
                ParcelIds.format(parcelId),
                door.no(),
                sizeClass,
                flat,
                now,
                code,
                expires);
    }
}
