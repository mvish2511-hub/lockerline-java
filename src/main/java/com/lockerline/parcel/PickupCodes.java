package com.lockerline.parcel;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.lockerline.config.LockerLineSettings;

/**
 * Pickup codes. Six random digits from a cryptographic source, stored only as a keyed hash (HmacSHA256 with
 * code-hash-key). Keyed and deterministic, so a presented code is found by one equality lookup, and a stolen table is
 * useless without the key; the final comparison is constant-time.
 */
@Component
public class PickupCodes {

    private final SecureRandom random = new SecureRandom();
    private final byte[] key;

    public PickupCodes(LockerLineSettings settings) {
        this.key = settings.codeHashKey().getBytes(StandardCharsets.UTF_8);
    }

    /** A new six-digit code, shown once in the drop response. */
    public String newCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    /** The keyed hash stored instead of the code. */
    public String hash(String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(code.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        }
    }

    /** Whether a presented code matches a stored hash, compared in constant time. */
    public boolean matches(String code, String storedHash) {
        return MessageDigest.isEqual(hash(code).getBytes(StandardCharsets.US_ASCII),
                storedHash.getBytes(StandardCharsets.US_ASCII));
    }
}
