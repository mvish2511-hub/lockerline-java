package com.lockerline.error;

import java.util.List;
import java.util.Map;

import com.lockerline.door.SizeClass;

/** 409 /errors/no-door-free: no free door of that size; freeSizes lists the sizes that are free. */
public class NoDoorFree extends DomainException {

    private final List<SizeClass> freeSizes;

    public NoDoorFree(SizeClass size, List<SizeClass> freeSizes) {
        super(409, "/errors/no-door-free", "No door free", "No " + size + " door is free");
        this.freeSizes = List.copyOf(freeSizes);
    }

    @Override
    public Map<String, Object> properties() {
        return Map.of("freeSizes", freeSizes);
    }
}
