package com.lockerline.error;

import java.util.Map;

/**
 * A refusal the domain gives: thrown by a service, turned into a ProblemDetail once, by {@link ApiExceptionHandler}.
 * Unchecked, so a refusal inside a transaction rolls it back.
 */
public abstract class DomainException extends RuntimeException {

    private final int status;
    private final String type;
    private final String title;

    protected DomainException(int status, String type, String title, String detail) {
        super(detail);
        this.status = status;
        this.type = type;
        this.title = title;
    }

    public int status() {
        return status;
    }

    public String type() {
        return type;
    }

    public String title() {
        return title;
    }

    /** Extra fields for this refusal's answer (none for NoSuchTower or NoSuchDoor). */
    public Map<String, Object> properties() {
        return Map.of();
    }
}

