package uk.co.compendiumdev.simpleapi;

import java.time.Instant;

/**
 * Utility that generates an ISBN‑13‑style string out of the current time
 * (epoch milliseconds).
 *
 * <p>
 * The format is {@code XXX-X-XX-XXXXXX-X} (e.g.
 * “999‑9‑99‑123456‑2”).  The digits come from the 13‑digit
 * representation of {@code System.currentTimeMillis()}.
 * </p>
 *
 * <pre>{@code
 *   System.out.println(TimestampToIsbn13.currentIsbn());
 *   // 1609459200000 → 160-9-45-920000-0
 * }</pre>
 */
public final class TimestampToIsbn13 {

    private TimestampToIsbn13() {}

    /**
     * Returns an ISBN‑13 style string made from the current timestamp
     * (epoch milliseconds).
     *
     * @return e.g. {@code 999-9-99-123456-2}
     */
    public static String currentIsbn() {
        // 13‑digit string built from System.currentTimeMillis().
        String epoch = String.format("%013d", Instant.now().toEpochMilli());

        // Split into the 5 groups: 3‑1‑2‑6‑1
        return epoch.substring(0, 3) + "-"          // 999
                + epoch.substring(3, 4) + "-"      // 9
                + epoch.substring(4, 6) + "-"      // 99
                + epoch.substring(6, 12) + "-"     // 123456
                + epoch.substring(12);             // 2
    }

    /**
     * Same as {@link #currentIsbn()}, but you can supply your own
     * {@code epochMillis} value. Useful for deterministic tests.
     *
     * @param epochMillis  the epoch millisecond value to convert
     * @return ISBN‑13 style string
     */
    public static String toIsbn(long epochMillis) {
        String epoch = String.format("%013d", epochMillis);
        return epoch.substring(0, 3) + "-" + epoch.substring(3, 4) + "-"
                + epoch.substring(4, 6) + "-" + epoch.substring(6, 12) + "-"
                + epoch.substring(12);
    }
}
