package com.lockerline.parcel;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A parcel's public id - P001, P017 - made from its database id, which is never shown itself. */
public final class ParcelIds {

    private static final Pattern PUBLIC = Pattern.compile("P(\\d+)");

    private ParcelIds() {
    }

    public static String format(long id) {
        return String.format("P%03d", id);
    }

    /** The database id behind a public id, or empty when it is not one. */
    public static Optional<Long> parse(String publicId) {
        Matcher m = PUBLIC.matcher(publicId);
        return m.matches() ? Optional.of(Long.parseLong(m.group(1))) : Optional.empty();
    }
}
