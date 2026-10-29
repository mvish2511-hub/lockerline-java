package com.lockerline.door;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lockerline.config.LockerLineSettings;

/**
 * Loads tower A, its 48 doors (seed/doors.csv) and its residents (seed/residents.csv) - once. A second start finds the
 * tower already there and adds nothing.
 */
@Service
public class SeedService {

    static final String TOWER = "A";

    private final TowerRepository towers;
    private final DoorRepository doors;
    private final ResidentRepository residents;
    private final LockerLineSettings settings;

    public SeedService(TowerRepository towers, DoorRepository doors, ResidentRepository residents,
                       LockerLineSettings settings) {
        this.towers = towers;
        this.doors = doors;
        this.residents = residents;
        this.settings = settings;
    }

    @Transactional
    public void seed() {
        if (towers.idOf(TOWER).isPresent()) {
            return;
        }
        long tower = towers.insert(TOWER, "Tower A");
        for (String[] row : csv("doors.csv")) {                 // no,size_class
            doors.insert(tower, row[0], SizeClass.valueOf(row[1]));
        }
        for (String[] row : csv("residents.csv")) {             // flat,name
            residents.insert(tower, row[0], row[1]);
        }
    }

    /** The rows of a seed file, header skipped, each split on commas. */
    private List<String[]> csv(String file) {
        try {
            return Files.readAllLines(Path.of(settings.seedDir(), file)).stream().skip(1)
                    .filter(line -> !line.isBlank()).map(line -> line.trim().split("\\s*,\\s*")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read the seed file " + file, e);
        }
    }
}
