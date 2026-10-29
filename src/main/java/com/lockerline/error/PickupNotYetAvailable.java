package com.lockerline.error;

/**
 * 400 /errors/domain: a valid code, but opening the door, collecting the parcel and freeing the door are not part
 * of this ticket, so a valid code is refused plainly instead of opening anything.
 */
public class PickupNotYetAvailable extends DomainException {

    public PickupNotYetAvailable() {
        super(400, "/errors/domain", "Refused", "This code is valid, but opening the door is not available yet");
    }
}
