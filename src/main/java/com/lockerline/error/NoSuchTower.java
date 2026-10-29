package com.lockerline.error;

/** 404 /errors/no-such-tower. */
public class NoSuchTower extends DomainException {

    public NoSuchTower(String tower) {
        super(404, "/errors/no-such-tower", "No such tower", "There is no tower " + tower);
    }
}
