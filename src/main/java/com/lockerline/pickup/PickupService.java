package com.lockerline.pickup;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lockerline.door.DoorRepository;
import com.lockerline.door.DoorService;
import com.lockerline.error.CodeExpired;
import com.lockerline.error.CodeUsed;
import com.lockerline.error.NoSuchCode;
import com.lockerline.parcel.ParcelIds;
import com.lockerline.parcel.ParcelRepository;
import com.lockerline.parcel.PickupCodes;

/**
 * A presented code is refused when it never existed (404 no-such-code) or its window has passed (404
 * code-expired). A valid code opens its door once (a second time is 409 code-used): the parcel is collected,
 * its dwell stamped from the clock, and the door freed - all in one transaction.
 */
@Service
public class PickupService {

    private final DoorService doorService;
    private final DoorRepository doors;
    private final ParcelRepository parcels;
    private final PickupCodes codes;
    private final Clock clock;

    public PickupService(DoorService doorService, DoorRepository doors, ParcelRepository parcels,
                         PickupCodes codes, Clock clock) {
        this.doorService = doorService;
        this.doors = doors;
        this.parcels = parcels;
        this.codes = codes;
        this.clock = clock;
    }

    @Transactional
    public PickupResponse pickup(String tower, String code) {
        long towerId = doorService.towerId(tower);

        ParcelRepository.CodeRow parcel = parcels.findByCodeHash(towerId, codes.hash(code))
                .filter(p -> codes.matches(code, p.codeHash()))
                .orElseThrow(NoSuchCode::new);

        if (parcel.codeUsedAt() != null) {
            throw new CodeUsed();
        }

        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);

        if (!now.isBefore(parcel.codeExpiresAt())) {
            throw new CodeExpired();
        }

        long dwell = Duration.between(parcel.droppedAt(), now).getSeconds();

        parcels.collect(parcel.id(), now, dwell);
        doors.free(parcel.doorId());

        return new PickupResponse(ParcelIds.format(parcel.id()), parcel.door(), now, dwell);
    }
}
