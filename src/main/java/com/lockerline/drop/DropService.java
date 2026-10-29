package com.lockerline.drop;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lockerline.door.DoorRepository;
import com.lockerline.door.DoorService;
import com.lockerline.door.ResidentRepository;
import com.lockerline.door.SizeClass;
import com.lockerline.error.NoDoorFree;
import com.lockerline.error.NoSuchFlat;
import com.lockerline.parcel.ParcelIds;
import com.lockerline.parcel.ParcelRepository;

/**
 * A courier books a drop. The door is a free door of exactly the parcel's size, lowest number first. Creating
 * the parcel and holding the door are one transaction: a failure part-way leaves neither.
 */
@Service
public class DropService {

    private final DoorService doorService;
    private final DoorRepository doors;
    private final ResidentRepository residents;
    private final ParcelRepository parcels;
    private final Clock clock;

    public DropService(DoorService doorService, DoorRepository doors, ResidentRepository residents,
                       ParcelRepository parcels, Clock clock) {
        this.doorService = doorService;
        this.doors = doors;
        this.residents = residents;
        this.parcels = parcels;
        this.clock = clock;
    }

    @Transactional
    public DropResponse drop(String tower, SizeClass size, String flat) {
        long towerId = doorService.towerId(tower);
        if (!residents.exists(towerId, flat)) {
            throw new NoSuchFlat(flat);
        }
        DoorRepository.Row door = doors.lowestFree(towerId, size)
                .orElseThrow(() -> new NoDoorFree(size, doors.freeSizes(towerId)));
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        long parcel = parcels.insert(towerId, flat, size, door.id(), now);
        doors.hold(door.id(), parcel);
        return new DropResponse(ParcelIds.format(parcel), door.no(), size, flat, now);
    }
}
