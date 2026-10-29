package com.lockerline.error;

/** 404 /errors/no-such-parcel. */
public class NoSuchParcel extends DomainException {

    public NoSuchParcel(String parcelId) {
        super(404, "/errors/no-such-parcel", "No such parcel", "There is no parcel " + parcelId);
    }
}
