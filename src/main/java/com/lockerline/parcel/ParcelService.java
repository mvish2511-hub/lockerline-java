package com.lockerline.parcel;

import org.springframework.stereotype.Service;

import com.lockerline.error.NoSuchParcel;

/** Reading one parcel. */
@Service
public class ParcelService {

    private final ParcelRepository parcels;

    public ParcelService(ParcelRepository parcels) {
        this.parcels = parcels;
    }

    public ParcelView parcel(String parcelId) {
        ParcelRepository.Row row = ParcelIds.parse(parcelId)
                .flatMap(parcels::find)
                .orElseThrow(() -> new NoSuchParcel(parcelId));

        return new ParcelView(
                ParcelIds.format(row.id()),
                row.door(),
                row.sizeClass(),
                row.flat(),
                row.state(),
                row.droppedAt()
        );
    }
}
