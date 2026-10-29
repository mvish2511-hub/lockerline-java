package com.lockerline.error;

/**
 * 404 /errors/code-expired: the code exists but its window has passed. The same status as no-such-code, so a wrong
 * code and an old one look alike to an attacker; the type differs, so the caretaker's screen can tell them apart.
 */
public class CodeExpired extends DomainException {

    public CodeExpired() {
        super(404, "/errors/code-expired", "Code expired", "That code's window has passed");
    }
}
