package com.lockerline.error;

/** 400 /errors/no-such-flat: the flat does not exist in this tower. */
public class NoSuchFlat extends DomainException {

    public NoSuchFlat(String flat) {
        super(400, "/errors/no-such-flat", "No such flat", "There is no flat " + flat + " in this tower");
    }
}
