package com.lockerline.error;

/** 409 /errors/code-used: the code has already opened its door once. */
public class CodeUsed extends DomainException {

    public CodeUsed() {
        super(409, "/errors/code-used", "Code already used", "That code has already opened its door");
    }
}
