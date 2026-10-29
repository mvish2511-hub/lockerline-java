package com.lockerline.door;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lockerline.error.NoSuchDoor;
import com.lockerline.error.NoSuchTower;

/** The wall: every door with its state, derived from what the door holds on every read. */
@Service
public class DoorService {

    private final TowerRepository towers;
    private final DoorRepository doors;

    public DoorService(TowerRepository towers, DoorRepository doors) {
        this.towers = towers;
        this.doors = doors;
    }

    /** The wall, optionally only one size and/or one state; `free` counts the free doors among those shown. */
    public WallView wall(String tower, SizeClass size, DoorState state) {
        long towerId = towerId(tower);
        List<WallDoor> shown = doors.all(towerId).stream()
                .map(row -> new WallDoor(row.no(), row.sizeClass(), stateOf(row)))
                .filter(d -> size == null || d.size() == size)
                .filter(d -> state == null || d.state() == state)
                .toList();
        int free = (int) shown.stream().filter(d -> d.state() == DoorState.free).count();
        return new WallView(tower, free, shown);
    }

public DoorDetail door(String tower, String no) {
        DoorRepository.Row row = doors.one(towerId(tower), no).orElseThrow(() -> new NoSuchDoor(tower, no));
        return new DoorDetail(row.no(), row.sizeClass(), stateOf(row));
    }

    private long towerId(String tower) {
        return towers.idOf(tower).orElseThrow(() -> new NoSuchTower(tower));
    }

    private static DoorState stateOf(DoorRepository.Row row) {
        return row.currentParcelId() == null
                ? DoorState.free
                : DoorState.holding;
    }
}
