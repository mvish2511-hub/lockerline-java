package com.lockerline.door;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/**
 * Seeds the wall once every bean exists and before Tomcat starts listening, so no request can see a half-loaded wall.
 * (An ApplicationRunner would run after the server is already accepting requests.)
 */
@Component
public class WallSeeder implements SmartInitializingSingleton {

    private final SeedService seed;

    public WallSeeder(SeedService seed) {
        this.seed = seed;
    }

    @Override
    public void afterSingletonsInstantiated() {
        seed.seed();
    }
}
