package com.lockerline.pickup;

import java.time.Clock;

import org.springframework.stereotype.Service;

import com.lockerline.door.DoorService;
import com.lockerline.error.CodeExpired;
import com.lockerline.error.NoSuchCode;
import com.lockerline.error.PickupNotYetAvailable;
import com.lockerline.parcel.ParcelRepository;
import com.lockerline.parcel.PickupCodes;

/**
 * A presented code is refused when it never existed (404 no-such-code) or its window has passed (404
 * code-expired). The rules live here; the one handler turns each refusal into its answer.
 */
@Service
public class PickupService {

    private final DoorService doorService;
    private final ParcelRepository parcels;
    private final PickupCodes codes;
    private final Clock clock;

    public PickupService(DoorService doorService, ParcelRepository parcels, PickupCodes codes, Clock clock) {
        this.doorService = doorService;
        this.parcels = parcels;
        this.codes = codes;
        this.clock = clock;
    }

    public void pickup(String tower, String code) {
        long towerId = doorService.towerId(tower);
        ParcelRepository.CodeRow parcel = parcels.findByCodeHash(towerId, codes.hash(code))
                .filter(p -> codes.matches(code, p.codeHash()))
                .orElseThrow(NoSuchCode::new);
        if (!clock.instant().isBefore(parcel.codeExpiresAt())) {
            throw new CodeExpired();
        }
        // a valid code: opening the door, collecting the parcel and freeing the door are not part of this ticket
        throw new PickupNotYetAvailable();
    }
}
