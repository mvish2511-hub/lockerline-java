package com.lockerline.error;

/** 404 /errors/no-such-code: the code does not exist in this tower. */
public class NoSuchCode extends DomainException {

    public NoSuchCode() {
        super(404, "/errors/no-such-code", "No such code", "That code does not exist");
    }
}
