/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.icu;

import com.ibm.icu.text.TimeZoneNames.NameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import libcore.util.BasicLruCache;
import libcore.util.ZoneInfoDB;

/**
 * Provides access to ICU's time zone name data.
 */
public final class TimeZoneNames {
    /** The name to use if a zone is a variation of UTC and ICU cannot provide a name. */
    private static final String UTC_NAME = "UTC";

    private static final int ID_LOOKUP_OLSON_NAME = 0;
    private static final int ID_LOOKUP_CANONICAL_ID = 1;
    /*
     * A set of all available timezone IDs and the associated canonical timezone ID recognized by
     * ICU4J. Although ICU4J caches the lookup itself, at the time of writing it uses a
     * SoftReference-based cache so it cannot be relied upon but produces rosy-looking benchmark
     * results that are unlikely to be repeated in real applications.
     */
    private static final String[][] availableTimeZoneIds;
    static {
        String[] tzIds = TimeZone.getAvailableIDs();
        String[][] canonicalIdLookup = new String[tzIds.length][2];
        for (int i = 0; i < tzIds.length; i++) {
            canonicalIdLookup[i][ID_LOOKUP_OLSON_NAME] = tzIds[i];
            canonicalIdLookup[i][ID_LOOKUP_CANONICAL_ID] =
                    com.ibm.icu.util.TimeZone.getCanonicalID(tzIds[i]);
        }
        availableTimeZoneIds = canonicalIdLookup;
    }

    /*
     * Offsets into the arrays returned by DateFormatSymbols.getZoneStrings.
     */
    public static final int OLSON_NAME = 0;
    public static final int LONG_NAME = 1;
    public static final int SHORT_NAME = 2;
    public static final int LONG_NAME_DST = 3;
    public static final int SHORT_NAME_DST = 4;
    public static final int NAME_COUNT = 5;

    private static final ZoneStringsCache cachedZoneStrings = new ZoneStringsCache();
    static {
        // Ensure that we pull in the zone strings for the root locale, en_US, and the
        // user's default locale. (All devices must support the root locale and en_US,
        // and they're used for various system things like HTTP headers.) Pre-populating
        // the cache is especially useful on Android because we'll share this via the Zygote.
        cachedZoneStrings.get(Locale.ROOT);
        cachedZoneStrings.get(Locale.US);
        cachedZoneStrings.get(Locale.getDefault());
    }

    public static class ZoneStringsCache extends BasicLruCache<Locale, String[][]> {
        public ZoneStringsCache() {
            super(5); // Room for a handful of locales.
        }

        @Override protected String[][] create(Locale locale) {
            return createZoneStrings(locale);
        }
    }

    private static final Comparator<String[]> ZONE_STRINGS_COMPARATOR = new Comparator<String[]>() {
        public int compare(String[] lhs, String[] rhs) {
            return lhs[OLSON_NAME].compareTo(rhs[OLSON_NAME]);
        }
    };

    private TimeZoneNames() {}

    /**
     * Returns the appropriate string from 'zoneStrings'. Used with getZoneStrings.
     */
    public static String getDisplayName(String[][] zoneStrings, String id, boolean daylight, int style) {
        String[] needle = new String[] { id };
        int index = Arrays.binarySearch(zoneStrings, needle, ZONE_STRINGS_COMPARATOR);
        if (index >= 0) {
            String[] row = zoneStrings[index];
            if (daylight) {
                return (style == TimeZone.LONG) ? row[LONG_NAME_DST] : row[SHORT_NAME_DST];
            } else {
                return (style == TimeZone.LONG) ? row[LONG_NAME] : row[SHORT_NAME];
            }
        }
        return null;
    }

    /**
     * Returns an array of time zone strings, as used by DateFormatSymbols.getZoneStrings.
     */
    public static String[][] getZoneStrings(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return cachedZoneStrings.get(locale);
    }

    /**
     * Returns an array containing the time zone ids in use in the country corresponding to
     * the given locale. This is not necessary for Java API, but is used by telephony as a
     * fallback. We retrieve these strings from zone.tab rather than icu4c because the latter
     * supplies them in alphabetical order where zone.tab has them in a kind of "importance"
     * order (as defined in the zone.tab header).
     */
    public static String[] forLocale(Locale locale) {
        String countryCode = locale.getCountry();
        ArrayList<String> ids = new ArrayList<String>();
        for (String line : ZoneInfoDB.getInstance().getZoneTab().split("\n")) {
            if (line.startsWith(countryCode)) {
                int olsonIdStart = line.indexOf('\t', 4) + 1;
                int olsonIdEnd = line.indexOf('\t', olsonIdStart);
                if (olsonIdEnd == -1) {
                    olsonIdEnd = line.length(); // Not all zone.tab lines have a comment.
                }
                ids.add(line.substring(olsonIdStart, olsonIdEnd));
            }
        }
        return ids.toArray(new String[ids.size()]);
    }

    public static String getExemplarLocation(Locale locale, TimeZone timeZone) {
        com.ibm.icu.text.TimeZoneNames timeZoneNames =
                com.ibm.icu.text.TimeZoneNames.getInstance(locale);
        long now = System.currentTimeMillis();
        String canonicalTimeZoneId = com.ibm.icu.util.TimeZone.getCanonicalID(timeZone.getID());
        String name =
                timeZoneNames.getDisplayName(canonicalTimeZoneId, NameType.EXEMPLAR_LOCATION, now);
        return name == null ? "" : name;
    }

    private static String[][] createZoneStrings(Locale locale) {
        long icuStart = System.currentTimeMillis();
        String[][] result = fillZoneStrings(icuStart, locale);
        long icuEnd = System.currentTimeMillis();

        internStrings(result);
        // Ending up in this method too often is an easy way to make your app slow, so we ensure
        // it's easy to tell from the log (a) what we were doing, (b) how long it took, and
        // (c) that it's all ICU's fault.
        long end = System.currentTimeMillis();
        long icuDuration = icuEnd - icuStart;
        long duration = end - icuStart;
        System.logI("Loaded time zone names for \"" + locale + "\" in " + duration + "ms" +
                " (" + icuDuration + "ms in ICU)");
        return result;
    }

    // De-duplicate the strings (http://b/2672057).
    private static synchronized void internStrings(String[][] result) {
        HashMap<String, String> internTable = new HashMap<String, String>();
        for (int i = 0; i < result.length; ++i) {
            for (int j = 1; j < NAME_COUNT; ++j) {
                String original = result[i][j];
                String nonDuplicate = internTable.get(original);
                if (nonDuplicate == null) {
                    internTable.put(original, original);
                } else {
                    result[i][j] = nonDuplicate;
                }
            }
        }
    }

    private static String[][] fillZoneStrings(long now, Locale locale) {
        // Set up the 2D array used to hold the names. The first column contains the Olson ids.
        String[][] result = new String[availableTimeZoneIds.length][NAME_COUNT];

        com.ibm.icu.text.TimeZoneNames timeZoneNames =
                com.ibm.icu.text.TimeZoneNames.getInstance(locale);
        for (int i = 0; i < result.length; i++) {
            String[] row = result[i];
            String zoneId = availableTimeZoneIds[i][ID_LOOKUP_OLSON_NAME];
            String canonicalZoneId = availableTimeZoneIds[i][ID_LOOKUP_CANONICAL_ID];

            row[OLSON_NAME] = zoneId;
            String longStd =
                    timeZoneNames.getDisplayName(canonicalZoneId, NameType.LONG_STANDARD, now);
            String shortStd =
                    timeZoneNames.getDisplayName(canonicalZoneId, NameType.SHORT_STANDARD, now);
            String longDst =
                    timeZoneNames.getDisplayName(canonicalZoneId, NameType.LONG_DAYLIGHT, now);
            String shortDst =
                    timeZoneNames.getDisplayName(canonicalZoneId, NameType.SHORT_DAYLIGHT, now);
            row[LONG_NAME] = filterGmt(longStd);
            row[SHORT_NAME] = filterGmt(shortStd);
            row[LONG_NAME_DST] = filterGmt(longDst);
            row[SHORT_NAME_DST] = filterGmt(shortDst);
            if (isUtc(zoneId)) {
                // If ICU doesn't give us a string for any zone that is a "UTC" variation, we supply
                // our own value instead of using "GMT". At the time of writing ICU does not provide
                // a value for any of these.
                if (row[LONG_NAME] == null) row[LONG_NAME] = UTC_NAME;
                if (row[SHORT_NAME] == null) row[SHORT_NAME] = UTC_NAME;
                if (row[LONG_NAME_DST] == null) row[LONG_NAME_DST] = UTC_NAME;
                if (row[SHORT_NAME_DST] == null) row[SHORT_NAME_DST] = UTC_NAME;
            }
        }
        return result;
    }

    private static String filterGmt(String name) {
        // We don't need to use ICU for GMT or GMT(+-) zones because TimeZone.getDisplayName creates
        // names on demand using the timezone data. This way we will never rely on ICU calculating
        // the zone offset correctly.
        if (name == null || name.startsWith("GMT")) {
            return null;
        }
        return name;
    }

    private static boolean isUtc(String zoneId) {
        // At the time of writing performing 8 string comparisons is faster than using a single
        // regexp by a significant margin.
        return "Etc/UCT".equals(zoneId)
                || "Etc/UTC".equals(zoneId)
                || "Etc/Universal".equals(zoneId)
                || "Etc/Zulu".equals(zoneId)
                || "UCT".equals(zoneId)
                || "UTC".equals(zoneId)
                || "Universal".equals(zoneId)
                || "Zulu".equals(zoneId);
    }
}
