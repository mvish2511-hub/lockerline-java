package com.lockerline.error;

/** 404 /errors/no-such-door. */
public class NoSuchDoor extends DomainException {

    public NoSuchDoor(String tower, String no) {
        super(404, "/errors/no-such-door", "No such door", "Tower " + tower + " has no door " + no);
    }
}
