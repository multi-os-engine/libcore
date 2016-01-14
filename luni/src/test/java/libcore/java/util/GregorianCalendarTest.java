/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package libcore.java.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import junit.framework.TestCase;

public class GregorianCalendarTest extends TestCase {

    private static final TimeZone LOS_ANGELES = TimeZone.getTimeZone("America/Los_Angeles");

    private static final TimeZone LONDON = TimeZone.getTimeZone("Europe/London");

    private static final int HOUR_IN_MILLIS = 3600000;

    private static int hours(double count) {
        return (int) (HOUR_IN_MILLIS * count);
    }

    private static final SimpleTimeZone CUSTOM_LOS_ANGELES_TIME_ZONE = new SimpleTimeZone(-28800000,
            "Custom America/Los_Angeles",
            Calendar.MARCH, 9, 0, hours(2),
            Calendar.NOVEMBER, 2, 0, hours(2),
            hours(1));

    // Documented a previous difference in behavior between this and the RI, see
    // https://code.google.com/p/android/issues/detail?id=61993 for more details.
    // Switching to OpenJDK has fixed that issue and so this test has been changed to reflect
    // the correct behavior.
    public void test_computeFields_dayOfWeekAndWeekOfYearSet() {
        Calendar greg = new GregorianCalendar(LOS_ANGELES, Locale.ENGLISH);

        // Ensure we use different values to the default ones.
        int differentWeekOfYear = greg.get(Calendar.WEEK_OF_YEAR) == 1 ? 2 : 1;
        int differentDayOfWeek = greg.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                ? Calendar.TUESDAY : Calendar.MONDAY;

        // Setting WEEK_OF_YEAR and DAY_OF_WEEK with an intervening
        // call to computeFields will work.
        greg.set(Calendar.WEEK_OF_YEAR, differentWeekOfYear);
        assertEquals(differentWeekOfYear, greg.get(Calendar.WEEK_OF_YEAR));
        greg.set(Calendar.DAY_OF_WEEK, differentDayOfWeek);
        assertEquals(differentWeekOfYear, greg.get(Calendar.WEEK_OF_YEAR));

        // Setting WEEK_OF_YEAR after DAY_OF_WEEK with no intervening
        // call to computeFields will work.
        greg = new GregorianCalendar(LOS_ANGELES, Locale.ENGLISH);
        greg.set(Calendar.DAY_OF_WEEK, differentDayOfWeek);
        greg.set(Calendar.WEEK_OF_YEAR, differentWeekOfYear);
        assertEquals(differentWeekOfYear, greg.get(Calendar.WEEK_OF_YEAR));
        assertEquals(differentDayOfWeek, greg.get(Calendar.DAY_OF_WEEK));

        // Setting DAY_OF_WEEK after WEEK_OF_YEAR with no intervening
        // call to computeFields will work.
        greg = new GregorianCalendar(LOS_ANGELES, Locale.ENGLISH);
        greg.set(Calendar.WEEK_OF_YEAR, differentWeekOfYear);
        greg.set(Calendar.DAY_OF_WEEK, differentDayOfWeek);
        assertEquals(differentWeekOfYear, greg.get(Calendar.WEEK_OF_YEAR));
        assertEquals(differentDayOfWeek, greg.get(Calendar.DAY_OF_WEEK));
    }

    /**
     * Specialized tests for those fields affected by GregorianCalendar cut over date.
     *
     * <p>Expands on a regression test created for harmony-2947.
     */
    public void test_fieldsAffectedByGregorianCutOver() {

        Date date = new Date(Date.parse("Jan 1 00:00:01 GMT 2000"));
        assertEquals(946684801000L, date.getTime());

        GregorianCalendar gc;

        // Test in America/Los_Angeles
        gc = new GregorianCalendar(LOS_ANGELES, Locale.ENGLISH);
        gc.setGregorianChange(date);
        gc.setTime(date);

        // Check the date to ensure that it is 18th Dec 1999. The reason this is not 1st Jan 2000
        // is that the offset for Los Angeles is GMT-08:00. setGregorianChange() is interpreted as
        // a wall time, not an instant. So, the instant that corresponds to
        // "1st Jan 2000 00:00:01 GMT" is 8 hours before the Julian/Gregorian switch in LA. The day
        // before 1st Jan 2000 (Gregorian Calendar) would be 18th Dec 1999 in the Julian calendar.
        // The reason it is not the 31st Dec 1999 is simply a result of the discontinuity that
        // occurred when switching calendars. That happened for real when the calendars were
        // switched in 1582.
        //
        // A different year explains why we get very different results for the methods being tested.
        assertEquals(1999, gc.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, gc.get(Calendar.MONTH));
        assertEquals(18, gc.get(Calendar.DAY_OF_MONTH));

        assertEquals(50, gc.getActualMaximum(Calendar.WEEK_OF_YEAR));
        assertEquals(50, gc.getLeastMaximum(Calendar.WEEK_OF_YEAR));
        assertEquals(3, gc.getActualMaximum(Calendar.WEEK_OF_MONTH));
        assertEquals(3, gc.getLeastMaximum(Calendar.WEEK_OF_MONTH));
        assertEquals(18, gc.getActualMaximum(Calendar.DAY_OF_MONTH));
        assertEquals(18, gc.getLeastMaximum(Calendar.DAY_OF_MONTH));
        assertEquals(352, gc.getActualMaximum(Calendar.DAY_OF_YEAR));
        assertEquals(352, gc.getLeastMaximum(Calendar.DAY_OF_YEAR));
        assertEquals(3, gc.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH));
        assertEquals(3, gc.getLeastMaximum(Calendar.DAY_OF_WEEK_IN_MONTH));

        // Test in Europe/London
        gc = new GregorianCalendar(LONDON, Locale.ENGLISH);
        gc.setGregorianChange(date);
        gc.setTime(date);

        // Check the date is actually 1st Jan 2000.
        assertEquals(2000, gc.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, gc.get(Calendar.MONTH));
        assertEquals(1, gc.get(Calendar.DAY_OF_MONTH));

        assertEquals(53, gc.getActualMaximum(Calendar.WEEK_OF_YEAR));
        assertEquals(52, gc.getLeastMaximum(Calendar.WEEK_OF_YEAR));
        assertEquals(5, gc.getActualMaximum(Calendar.WEEK_OF_MONTH));
        assertEquals(4, gc.getLeastMaximum(Calendar.WEEK_OF_MONTH));
        assertEquals(31, gc.getActualMaximum(Calendar.DAY_OF_MONTH));
        assertEquals(28, gc.getLeastMaximum(Calendar.DAY_OF_MONTH));
        assertEquals(366, gc.getActualMaximum(Calendar.DAY_OF_YEAR));
        assertEquals(365, gc.getLeastMaximum(Calendar.DAY_OF_YEAR));
        assertEquals(5, gc.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH));
        assertEquals(4, gc.getLeastMaximum(Calendar.DAY_OF_WEEK_IN_MONTH));
    }

    public void test_computeTime_enteringDST_TimeZone_LosAngeles_2014() {
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        checkDSTLosAngeles2014(timeZone);
    }

    /**
     * This test will fail in the RI (standard OpenJDK, i.e. not enso).
     *
     * <p>The behaviour is different in enso for backwards compatibility with previous versions of
     * Android. The behaviour in OpenJDK is considered to be a bug and an issue will be raised with
     * them.
     *
     * <p>Search for 'OpenJDK Failure' to see more details.
     */
    public void test_computeTime_enteringDST_DelegatingTimeZone_LosAngeles_2014() {
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        timeZone = new DelegatingTimeZone(timeZone);
        checkDSTLosAngeles2014(timeZone);
    }

    /**
     * This test will fail in the RI (standard OpenJDK, i.e. not enso).
     *
     * <p>The behaviour is different in enso for backwards compatibility with previous versions of
     * Android. The behaviour in OpenJDK is considered to be a bug and an issue will be raised with
     * them.
     *
     * <p>Search for 'OpenJDK Failure' to see more details.
     */
    public void test_computeTime_enteringDST_SimpleTimeZone_LosAngeles_2014() {
        checkDSTLosAngeles2014(CUSTOM_LOS_ANGELES_TIME_ZONE);
    }

    private void checkDSTLosAngeles2014(TimeZone timeZone) {
        Calendar cal = new GregorianCalendar(timeZone, Locale.ENGLISH);
        // Clear the milliseconds field.
        cal.set(Calendar.MILLISECOND, 0);

        String description;

        // Check milliseconds one second before the transition.
        description = "01:59:59 - March 9th 2014";
        cal.set(2014, Calendar.MARCH, 9, 1, 59, 59);
        checkMillis(cal, description, 1394359199000L);

        // Outside DST time.
        checkOutsideDST(cal, description);

        // Check milliseconds at the transition point but using an invalid wall clock
        // (02:00 - 02:59:59.999) do not actually exist.
        description = "02:00:00 - March 9th 2014";
        cal.set(2014, Calendar.MARCH, 9, 2, 0, 0);

        // OpenJDK Failure:
        //   This fails on OpenJDK when running with SimpleTimeZone (or any custom TimeZone
        //   implementation). It incorrectly calculates the time in millis to be 1394355600000.
        //   That is because GregorianCalendar treats the implementation that underpins
        //   TimeZone.getTimeZone(String) specially and the code that runs for other classes does
        //   not handle the invalid wall clock period on entry to DST properly.
        checkMillis(cal, description, 1394359200000L);

        // Invalid wall clock but treated as being inside DST time.
        checkInsideDST(cal, description);

        // Check milliseconds at the first valid wall clock time after transition, 03:00 - should
        // be treated the same as 02:00.
        description = "03:00:00 - March 9th 2014";
        cal.set(2014, Calendar.MARCH, 9, 3, 0, 0);
        checkMillis(cal, description, 1394359200000L);

        // Valid wall clock treated as being inside DST time.
        checkInsideDST(cal, description);

        // Check milliseconds at the last invalid wall clock time, 02:59:59.999.
        description = "02:59:59.999 - March 9th 2014";
        cal.set(2014, Calendar.MARCH, 9, 2, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        checkMillis(cal, description, 1394362799999L);

        // Invalid wall clock but treated as being inside DST time.
        checkInsideDST(cal, description);

        // Check milliseconds at 03:59:59.999 - should be treated the same as 02:59:59.999
        description = "03:59:59.999 - March 9th 2014";
        cal.set(2014, Calendar.MARCH, 9, 3, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        checkMillis(cal, description, 1394362799999L);

        // Valid wall clock treated as being inside DST time.
        checkInsideDST(cal, description);
    }

    private void checkMillis(Calendar cal, String description, long expectedMillis) {
        assertEquals("Incorrect millis: " + description, expectedMillis, cal.getTimeInMillis());
    }

    private void checkOutsideDST(Calendar cal, String description) {
        TimeZone timeZone = cal.getTimeZone();
        checkOutsideDST(cal, description, timeZone.getRawOffset());
    }

    private void checkOutsideDST(Calendar cal, String description, int expectedZoneOffset) {
        checkDSTFields(cal, description, expectedZoneOffset, 0);
    }

    private void checkInsideDST(Calendar cal, String description) {
        TimeZone timeZone = cal.getTimeZone();
        checkDSTFields(cal, description, timeZone.getRawOffset(), timeZone.getDSTSavings());
    }

    private void checkDSTFields(Calendar cal, String description, int expectedZoneOffset, int expectedDstOffset) {
        assertEquals("Incorrect ZONE_OFFSET: " + description, expectedZoneOffset, cal.get(Calendar.ZONE_OFFSET));
        assertEquals("Incorrect DST_OFFSET: " + description, expectedDstOffset, cal.get(Calendar.DST_OFFSET));
    }

    /**
     * A custom {@link TimeZone} implementation.
     */
    private static class DelegatingTimeZone extends TimeZone {

        private final TimeZone timeZone;

        public DelegatingTimeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek,
                int milliseconds) {
            return timeZone.getOffset(era, year, month, day, dayOfWeek, milliseconds);
        }

        @Override
        public int getOffset(long date) {
            return timeZone.getOffset(date);
        }

        @Override
        public void setRawOffset(int offsetMillis) {
            timeZone.setRawOffset(offsetMillis);
        }

        @Override
        public int getRawOffset() {
            return timeZone.getRawOffset();
        }

        @Override
        public String getID() {
            return timeZone.getID();
        }

        @Override
        public void setID(String ID) {
            timeZone.setID(ID);
        }

        @Override
        public String getDisplayName(boolean daylightTime, int style, Locale locale) {
            return timeZone.getDisplayName(daylightTime, style, locale);
        }

        @Override
        public int getDSTSavings() {
            return timeZone.getDSTSavings();
        }

        @Override
        public boolean useDaylightTime() {
            return timeZone.useDaylightTime();
        }

        @Override
        public boolean observesDaylightTime() {
            return timeZone.observesDaylightTime();
        }

        @Override
        public boolean inDaylightTime(Date date) {
            return timeZone.inDaylightTime(date);
        }

        @Override
        public boolean hasSameRules(TimeZone other) {
            return timeZone.hasSameRules(other);
        }
    }
}
