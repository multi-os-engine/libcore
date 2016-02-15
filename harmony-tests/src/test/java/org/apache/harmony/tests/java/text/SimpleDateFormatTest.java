/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.tests.java.text;

import sun.util.calendar.Gregorian;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;


public class SimpleDateFormatTest extends junit.framework.TestCase {

    private SimpleDateFormat format;

    private SimpleDateFormat pFormat;

    private TimeZone previousDefaultTimeZone;

    @Override public void setUp() {
        previousDefaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
        format = new SimpleDateFormat("", Locale.ENGLISH);
        pFormat = new SimpleDateFormat("", Locale.ENGLISH);
    }

    @Override public void tearDown() {
        TimeZone.setDefault(previousDefaultTimeZone);
    }

    public void test_Constructor() {
        // Test for method java.text.SimpleDateFormat()
        SimpleDateFormat f2 = new SimpleDateFormat();
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertTrue("Wrong default", f2.equals(DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())));
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(new DateFormatSymbols()));
        assertTrue("Doesn't work", f2.format(new Date()).getClass() == String.class);
    }

    public void test_ConstructorLjava_lang_String() {
        // Test for method java.text.SimpleDateFormat(java.lang.String)
        SimpleDateFormat f2 = new SimpleDateFormat("yyyy");
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertEquals("Wrong pattern", "yyyy", f2.toPattern());
        assertTrue("Wrong locale", f2.equals(new SimpleDateFormat("yyyy", Locale.getDefault())));
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(new DateFormatSymbols()));
        assertTrue("Doesn't work", f2.format(new Date()).getClass() == String.class);

        // Invalid constructor value.
        try {
            new SimpleDateFormat("this is an invalid simple date format");
            fail("Expected test_ConstructorLjava_lang_String to throw IAE.");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        // Null string value
        try {
            new SimpleDateFormat(null);
            fail("Expected test_ConstructorLjava_lang_String to throw NPE.");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void test_ConstructorLjava_lang_StringLjava_text_DateFormatSymbols() {
        // Test for method java.text.SimpleDateFormat(java.lang.String,
        // java.text.DateFormatSymbols)
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.ENGLISH);
        symbols.setEras(new String[] { "Before", "After" });
        SimpleDateFormat f2 = new SimpleDateFormat("y'y'yy", symbols);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertEquals("Wrong pattern", "y'y'yy", f2.toPattern());
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(symbols));
        assertTrue("Doesn't work", f2.format(new Date()).getClass() == String.class);

        try {
            new SimpleDateFormat(null, symbols);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            new SimpleDateFormat("eee", symbols);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_ConstructorLjava_lang_StringLjava_util_Locale() {
        // Test for method java.text.SimpleDateFormat(java.lang.String,
        // java.util.Locale)
        SimpleDateFormat f2 = new SimpleDateFormat("'yyyy' MM yy", Locale.GERMAN);
        assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
        assertEquals("Wrong pattern", "'yyyy' MM yy", f2.toPattern());
        assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
                new DateFormatSymbols(Locale.GERMAN)));
        assertTrue("Doesn't work", f2.format(new Date()).getClass() == String.class);

        try {
            new SimpleDateFormat(null, Locale.GERMAN);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            new SimpleDateFormat("eee", Locale.GERMAN);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_applyLocalizedPatternLjava_lang_String() {
        SimpleDateFormat f2 = new SimpleDateFormat("y", new Locale("de", "CH"));
        String pattern = "GyMdkHmsSEDFwWahKzZLc";
        f2.applyLocalizedPattern(pattern);
        assertEquals(pattern, f2.toPattern());
        assertEquals(pattern, f2.toLocalizedPattern());

        // test invalid patterns
        try {
            f2.applyLocalizedPattern("b");
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            f2.applyLocalizedPattern("a '"); // Unterminated quote.
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            f2.applyLocalizedPattern(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_applyPatternLjava_lang_String() {
        // Test for method void
        // java.text.SimpleDateFormat.applyPattern(java.lang.String)
        SimpleDateFormat f2 = new SimpleDateFormat("y", new Locale("de", "CH"));
        f2.applyPattern("GyMdkHmsSEDFwWahKz");
        assertEquals("Wrong pattern", "GyMdkHmsSEDFwWahKz", f2.toPattern());

        // test invalid patterns
        try {
            f2.applyPattern("b");
            fail("Expected IllegalArgumentException for pattern with invalid patter letter: b");
        } catch (IllegalArgumentException e) {
        }

//        try {
//            f2.applyPattern("u");
//            fail("Expected IllegalArgumentException for pattern with invalid patter letter: u");
//        } catch (IllegalArgumentException e) {
//        }

        try {
            f2.applyPattern("a '");
            fail("Expected IllegalArgumentException for pattern with unterminated quote: a '");
        } catch (IllegalArgumentException e) {
        }

        try {
            f2.applyPattern(null);
            fail("Expected NullPointerException for null pattern");
        } catch (NullPointerException e) {
        }
    }

    public void test_clone() {
        // Test for method java.lang.Object java.text.SimpleDateFormat.clone()
        SimpleDateFormat f2 = new SimpleDateFormat();
        SimpleDateFormat clone = (SimpleDateFormat) f2.clone();
        assertTrue("Invalid clone", f2.equals(clone));
        clone.applyPattern("y");
        assertTrue("Format modified", !f2.equals(clone));
        clone = (SimpleDateFormat) f2.clone();
        // Date date = clone.get2DigitYearStart();
        // date.setTime(0);
        // assertTrue("Equal after date change: " +
        // f2.get2DigitYearStart().getTime() + " " +
        // clone.get2DigitYearStart().getTime(), !f2.equals(clone));
    }

    public void test_equalsLjava_lang_Object() {
        // Test for method boolean
        // java.text.SimpleDateFormat.equals(java.lang.Object)
        SimpleDateFormat format = (SimpleDateFormat) DateFormat.getInstance();
        SimpleDateFormat clone = (SimpleDateFormat) format.clone();
        assertTrue("clone not equal", format.equals(clone));
        format.format(new Date());
        assertTrue("not equal after format", format.equals(clone));
    }

    public void test_equals_afterFormat() {
        // Regression test for HARMONY-209
        SimpleDateFormat df = new SimpleDateFormat();
        df.format(new Date());
        assertEquals(df, new SimpleDateFormat());
      }

    public void test_hashCode() {
        SimpleDateFormat format = (SimpleDateFormat) DateFormat.getInstance();
        SimpleDateFormat clone = (SimpleDateFormat) format.clone();
        assertTrue("clone has not equal hash code", clone.hashCode() == format.hashCode());
        format.format(new Date());
        assertTrue("clone has not equal hash code after format",
                clone.hashCode() == format.hashCode());
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.ENGLISH);
        symbols.setEras(new String[] { "Before", "After" });
        SimpleDateFormat format2 = new SimpleDateFormat("y'y'yy", symbols);
        assertFalse("objects has equal hash code", format2.hashCode() == format.hashCode());
    }

    public void test_formatToCharacterIteratorLjava_lang_Object() {
        try {
            // Regression for HARMONY-466
            new SimpleDateFormat().formatToCharacterIterator(null);
            fail();
        } catch (NullPointerException expected) {
        }

        // Test for method formatToCharacterIterator(java.lang.Object)
        new Support_SimpleDateFormat(
                "test_formatToCharacterIteratorLjava_lang_Object")
                .t_formatToCharacterIterator();
    }

    public void test_formatLjava_util_DateLjava_lang_StringBufferLjava_text_FieldPosition() {
        // Test for method java.lang.StringBuffer
        // java.text.SimpleDateFormat.format(java.util.Date,
        // java.lang.StringBuffer, java.text.FieldPosition)

        new Support_SimpleDateFormat(
                "test_formatLjava_util_DateLjava_lang_StringBufferLjava_text_FieldPosition")
                .t_format_with_FieldPosition();

        Calendar cal = new GregorianCalendar(1999, Calendar.JUNE, 2, 15, 3, 6);
        assertFormat(" G", cal, " AD", DateFormat.ERA_FIELD);
        assertFormat(" GG", cal, " AD", DateFormat.ERA_FIELD);
        assertFormat(" GGG", cal, " AD", DateFormat.ERA_FIELD);
        assertFormat(" G", new GregorianCalendar(-1999, Calendar.JUNE, 2), " BC",
                DateFormat.ERA_FIELD);

        // This assumes Unicode behavior where 'y' and 'yyy' don't truncate,
        // which means that it will fail on the RI.
        assertFormat(" y", cal, " 1999", DateFormat.YEAR_FIELD);
        assertFormat(" yy", cal, " 99", DateFormat.YEAR_FIELD);
        assertFormat(" yy", new GregorianCalendar(2001, Calendar.JUNE, 2), " 01",
                DateFormat.YEAR_FIELD);
        assertFormat(" yy", new GregorianCalendar(2000, Calendar.JUNE, 2), " 00",
                DateFormat.YEAR_FIELD);
        assertFormat(" yyy", new GregorianCalendar(2000, Calendar.JUNE, 2), " 2000",
                DateFormat.YEAR_FIELD);
        assertFormat(" yyy", cal, " 1999", DateFormat.YEAR_FIELD);
        assertFormat(" yyyy", cal, " 1999", DateFormat.YEAR_FIELD);
        assertFormat(" yyyyy", cal, " 01999", DateFormat.YEAR_FIELD);

        assertFormat(" M", cal, " 6", DateFormat.MONTH_FIELD);
        assertFormat(" M", new GregorianCalendar(1999, Calendar.NOVEMBER, 2), " 11",
                DateFormat.MONTH_FIELD);
        assertFormat(" MM", cal, " 06", DateFormat.MONTH_FIELD);
        assertFormat(" MMM", cal, " Jun", DateFormat.MONTH_FIELD);
        assertFormat(" MMMM", cal, " June", DateFormat.MONTH_FIELD);
        assertFormat(" MMMMM", cal, " J", DateFormat.MONTH_FIELD);

        assertFormat(" d", cal, " 2", DateFormat.DATE_FIELD);
        assertFormat(" d", new GregorianCalendar(1999, Calendar.NOVEMBER, 12), " 12",
                DateFormat.DATE_FIELD);
        assertFormat(" dd", cal, " 02", DateFormat.DATE_FIELD);
        assertFormat(" dddd", cal, " 0002", DateFormat.DATE_FIELD);

        assertFormat(" h", cal, " 3", DateFormat.HOUR1_FIELD);
        assertFormat(" h", new GregorianCalendar(1999, Calendar.NOVEMBER, 12), " 12",
                DateFormat.HOUR1_FIELD);
        assertFormat(" hh", cal, " 03", DateFormat.HOUR1_FIELD);
        assertFormat(" hhhh", cal, " 0003", DateFormat.HOUR1_FIELD);

        assertFormat(" H", cal, " 15", DateFormat.HOUR_OF_DAY0_FIELD);
        assertFormat(" H", new GregorianCalendar(1999, Calendar.NOVEMBER, 12, 4, 0), " 4",
                DateFormat.HOUR_OF_DAY0_FIELD);
        assertFormat(" H", new GregorianCalendar(1999, Calendar.NOVEMBER, 12, 12, 0), " 12",
                DateFormat.HOUR_OF_DAY0_FIELD);
        assertFormat(" H", new GregorianCalendar(1999, Calendar.NOVEMBER, 12), " 0",
                DateFormat.HOUR_OF_DAY0_FIELD);
        assertFormat(" HH", cal, " 15", DateFormat.HOUR_OF_DAY0_FIELD);
        assertFormat(" HHHH", cal, " 0015", DateFormat.HOUR_OF_DAY0_FIELD);

        assertFormat(" m", cal, " 3", DateFormat.MINUTE_FIELD);
        assertFormat(" m", new GregorianCalendar(1999, Calendar.NOVEMBER, 12, 4, 47), " 47",
                DateFormat.MINUTE_FIELD);
        assertFormat(" mm", cal, " 03", DateFormat.MINUTE_FIELD);
        assertFormat(" mmmm", cal, " 0003", DateFormat.MINUTE_FIELD);

        assertFormat(" s", cal, " 6", DateFormat.SECOND_FIELD);
        assertFormat(" s", new GregorianCalendar(1999, Calendar.NOVEMBER, 12, 4, 47, 13), " 13",
                DateFormat.SECOND_FIELD);
        assertFormat(" ss", cal, " 06", DateFormat.SECOND_FIELD);
        assertFormat(" ssss", cal, " 0006", DateFormat.SECOND_FIELD);

        assertFormat(" S", cal, " 0", DateFormat.MILLISECOND_FIELD);
        Calendar temp = new GregorianCalendar();
        temp.set(Calendar.MILLISECOND, 961);

        assertFormat(" SS", temp, " 96", DateFormat.MILLISECOND_FIELD);
        assertFormat(" SSSS", cal, " 0000", DateFormat.MILLISECOND_FIELD);

        assertFormat(" SS", cal, " 00", DateFormat.MILLISECOND_FIELD);

        assertFormat(" E", cal, " Wed", DateFormat.DAY_OF_WEEK_FIELD);
        assertFormat(" EE", cal, " Wed", DateFormat.DAY_OF_WEEK_FIELD);
        assertFormat(" EEE", cal, " Wed", DateFormat.DAY_OF_WEEK_FIELD);
        assertFormat(" EEEE", cal, " Wednesday", DateFormat.DAY_OF_WEEK_FIELD);
        assertFormat(" EEEEE", cal, " W", DateFormat.DAY_OF_WEEK_FIELD);

        assertFormat(" D", cal, " 153", DateFormat.DAY_OF_YEAR_FIELD);
        assertFormat(" DD", cal, " 153", DateFormat.DAY_OF_YEAR_FIELD);
        assertFormat(" DDDD", cal, " 0153", DateFormat.DAY_OF_YEAR_FIELD);

        assertFormat(" F", cal, " 1", DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD);
        assertFormat(" F", new GregorianCalendar(1999, Calendar.NOVEMBER, 14), " 2",
                DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD);
        assertFormat(" FF", cal, " 01", DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD);
        assertFormat(" FFFF", cal, " 0001", DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD);

        cal.setMinimalDaysInFirstWeek(1);
        cal.setFirstDayOfWeek(1);

        assertFormat(" w", cal, " 23", DateFormat.WEEK_OF_YEAR_FIELD);
        assertFormat(" ww", cal, " 23", DateFormat.WEEK_OF_YEAR_FIELD);
        assertFormat(" wwww", cal, " 0023", DateFormat.WEEK_OF_YEAR_FIELD);

        assertFormat(" W", cal, " 1", DateFormat.WEEK_OF_MONTH_FIELD);
        assertFormat(" WW", cal, " 01", DateFormat.WEEK_OF_MONTH_FIELD);
        assertFormat(" WWWW", cal, " 0001", DateFormat.WEEK_OF_MONTH_FIELD);

        assertFormat(" a", cal, " PM", DateFormat.AM_PM_FIELD);
        assertFormat(" a", new GregorianCalendar(1999, Calendar.NOVEMBER, 14), " AM",
                DateFormat.AM_PM_FIELD);
        assertFormat(" a", new GregorianCalendar(1999, Calendar.NOVEMBER, 14, 12, 0), " PM",
                DateFormat.AM_PM_FIELD);
        assertFormat(" aa", cal, " PM", DateFormat.AM_PM_FIELD);
        assertFormat(" aaa", cal, " PM", DateFormat.AM_PM_FIELD);
        assertFormat(" aaaa", cal, " PM", DateFormat.AM_PM_FIELD);
        assertFormat(" aaaaa", cal, " PM", DateFormat.AM_PM_FIELD);

        assertFormat(" k", cal, " 15", DateFormat.HOUR_OF_DAY1_FIELD);
        assertFormat(" k", new GregorianCalendar(1999, Calendar.NOVEMBER, 12, 4, 0), " 4",
                DateFormat.HOUR_OF_DAY1_FIELD);
        assertFormat(" k", new GregorianCalendar(1999, Calendar.NOVEMBER, 12, 12, 0), " 12",
                DateFormat.HOUR_OF_DAY1_FIELD);
        assertFormat(" k", new GregorianCalendar(1999, Calendar.NOVEMBER, 12), " 24",
                DateFormat.HOUR_OF_DAY1_FIELD);
        assertFormat(" kk", cal, " 15", DateFormat.HOUR_OF_DAY1_FIELD);
        assertFormat(" kkkk", cal, " 0015", DateFormat.HOUR_OF_DAY1_FIELD);

        assertFormat(" K", cal, " 3", DateFormat.HOUR0_FIELD);
        assertFormat(" K", new GregorianCalendar(1999, Calendar.NOVEMBER, 12), " 0",
                DateFormat.HOUR0_FIELD);
        assertFormat(" KK", cal, " 03", DateFormat.HOUR0_FIELD);
        assertFormat(" KKKK", cal, " 0003", DateFormat.HOUR0_FIELD);

        format.applyPattern("'Mkz''':.@5");
        assertEquals("Wrong output", "Mkz':.@5", format.format(new Date()));

        // Test invalid args to format.
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        try {
            dateFormat.format(null, new StringBuffer(), new FieldPosition(1));
            fail();
        } catch (NullPointerException expected) {
        }
    }

    private void assertFormat(String pattern, Calendar cal, String expected, int field) {
        StringBuffer buffer = new StringBuffer();
        FieldPosition position = new FieldPosition(field);
        format.applyPattern(pattern);
        format.format(cal.getTime(), buffer, position);
        String result = buffer.toString();
        assertTrue("Wrong format: \"" + pattern + "\" expected: " + expected + " result: " + result,
                result.equals(expected));
        assertEquals("Wrong begin position: " + pattern + "\n" + "expected: " + expected + "\n" +
                "field: " + field, 1, position.getBeginIndex());
        assertTrue("Wrong end position: " + pattern + " expected: " + expected + " field: " + field,
                position.getEndIndex() == result.length());
    }

    public void test_format_time_zones() throws Exception {
        Calendar cal = new GregorianCalendar(1999, Calendar.JUNE, 2, 15, 3, 6);

        format.setTimeZone(TimeZone.getTimeZone("EST"));
        assertFormat(" z", cal, " GMT-05:00", DateFormat.TIMEZONE_FIELD);
        Calendar temp2 = new GregorianCalendar(1999, Calendar.JANUARY, 12);
        assertFormat(" z", temp2, " GMT-05:00", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zz", cal, " GMT-05:00", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zzz", cal, " GMT-05:00", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zzzz", cal, " GMT-05:00", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zzzz", temp2, " GMT-05:00", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zzzzz", cal, " GMT-05:00", DateFormat.TIMEZONE_FIELD);

        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        assertFormat(" z", cal, " EDT", DateFormat.TIMEZONE_FIELD);
        assertFormat(" z", temp2, " EST", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zz", cal, " EDT", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zzz", cal, " EDT", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zzzz", cal, " Eastern Daylight Time", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zzzz", temp2, " Eastern Standard Time", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zzzzz", cal, " Eastern Daylight Time", DateFormat.TIMEZONE_FIELD);

        TimeZone tz0001 = new SimpleTimeZone(60000, "ONE MINUTE");
        TimeZone tz0130 = new SimpleTimeZone(5400000, "ONE HOUR, THIRTY");
        TimeZone tzMinus0130 = new SimpleTimeZone(-5400000, "NEG ONE HOUR, THIRTY");

        format.setTimeZone(tz0001);
//        test(" Z", cal, " +0001", DateFormat.TIMEZONE_FIELD);
//        test(" ZZZZ", cal, " GMT+00:01", DateFormat.TIMEZONE_FIELD);
//        test(" ZZZZZ", cal, " +00:01", DateFormat.TIMEZONE_FIELD);
        format.setTimeZone(tz0130);
//        test(" Z", cal, " +0130", DateFormat.TIMEZONE_FIELD);
        format.setTimeZone(tzMinus0130);
//        test(" Z", cal, " -0130", DateFormat.TIMEZONE_FIELD);

        format.setTimeZone(tz0001);
        assertFormat(" z", cal, " GMT+00:01", DateFormat.TIMEZONE_FIELD);
        assertFormat(" zzzz", cal, " GMT+00:01", DateFormat.TIMEZONE_FIELD);
        format.setTimeZone(tz0130);
        assertFormat(" z", cal, " GMT+01:30", DateFormat.TIMEZONE_FIELD);
        format.setTimeZone(tzMinus0130);
        assertFormat(" z", cal, " GMT-01:30", DateFormat.TIMEZONE_FIELD);
    }

    public void test_timeZoneFormatting() {
        // tests specific to formatting of timezones
        Date summerDate = new GregorianCalendar(1999, Calendar.JUNE, 2, 15, 3, 6).getTime();
        Date winterDate = new GregorianCalendar(1999, Calendar.JANUARY, 12).getTime();

        verifyFormatTimezone(
                "America/Los_Angeles", "PDT, Pacific Daylight Time", "-0700, GMT-07:00",
                summerDate);
        verifyFormatTimezone(
                "America/Los_Angeles", "PST, Pacific Standard Time", "-0800, GMT-08:00",
                winterDate);

        verifyFormatTimezone("GMT-7", "GMT-07:00, GMT-07:00", "-0700, GMT-07:00", summerDate);
        verifyFormatTimezone("GMT-7", "GMT-07:00, GMT-07:00", "-0700, GMT-07:00", winterDate);

        verifyFormatTimezone("GMT+14", "GMT+14:00, GMT+14:00", "+1400, GMT+14:00", summerDate);
        verifyFormatTimezone("GMT+14", "GMT+14:00, GMT+14:00", "+1400, GMT+14:00", winterDate);

        // this fails on the RI!
        verifyFormatTimezone("America/Detroit", "EDT, Eastern Daylight Time", "-0400, GMT-04:00",
                summerDate);
        verifyFormatTimezone("America/Detroit", "EST, Eastern Standard Time", "-0500, GMT-05:00",
                winterDate);

        // Pacific/Kiritimati is one of the timezones supported only in mJava
        verifyFormatTimezone(
                "Pacific/Kiritimati", "GMT+14:00, Line Islands Time", "+1400, GMT+14:00",
                summerDate);
        verifyFormatTimezone(
                "Pacific/Kiritimati", "GMT+14:00, Line Islands Time", "+1400, GMT+14:00",
                winterDate);

        verifyFormatTimezone("EST", "GMT-05:00, GMT-05:00", "-0500, GMT-05:00", summerDate);
        verifyFormatTimezone("EST", "GMT-05:00, GMT-05:00", "-0500, GMT-05:00", winterDate);

        verifyFormatTimezone("GMT+14", "GMT+14:00, GMT+14:00", "+1400, GMT+14:00", summerDate);
        verifyFormatTimezone("GMT+14", "GMT+14:00, GMT+14:00", "+1400, GMT+14:00", winterDate);
    }

    private void verifyFormatTimezone(String timeZoneId, String expected1, String expected2,
            Date date) {
        format.setTimeZone(SimpleTimeZone.getTimeZone(timeZoneId));
        format.applyPattern("z, zzzz");
        assertEquals("Test z for TimeZone : " + timeZoneId, expected1, format.format(date));

        format.applyPattern("Z, ZZZZ");
        assertEquals("Test Z for TimeZone : " + timeZoneId, expected2, format.format(date));
    }

    public void test_get2DigitYearStart() {
        // Test for method java.util.Date
        // java.text.SimpleDateFormat.get2DigitYearStart()
        SimpleDateFormat f1 = new SimpleDateFormat("y");
        Date date = f1.get2DigitYearStart();
        Calendar cal = new GregorianCalendar();
        int year = cal.get(Calendar.YEAR);
        cal.setTime(date);
        assertTrue("Wrong default year start", cal.get(Calendar.YEAR) == (year - 80));
    }

    public void test_getDateFormatSymbols() {
        // Test for method java.text.DateFormatSymbols
        // java.text.SimpleDateFormat.getDateFormatSymbols()
        SimpleDateFormat df = (SimpleDateFormat) DateFormat.getInstance();
        DateFormatSymbols dfs = df.getDateFormatSymbols();
        assertTrue("Symbols identical", dfs != df.getDateFormatSymbols());
    }

    public void test_parseLjava_lang_StringLjava_text_ParsePosition() throws Exception {
        // Test for method java.util.Date
        // java.text.SimpleDateFormat.parse(java.lang.String,
        // java.text.ParsePosition)
        Calendar cal = new GregorianCalendar(1970, Calendar.JANUARY, 1);
        Date time = cal.getTime();
        assertParse("h", " 12", time, 1, 3);
        assertParse("H", " 0", time, 1, 2);
        assertParse("k", " 24", time, 1, 3);
        assertParse("K", " 0", time, 1, 2);

        cal = new GregorianCalendar(1970, Calendar.JANUARY, 1, 1, 0);
        time = cal.getTime();
        assertParse("h", "1", time, 0, 1);
        assertParse("H", "1 ", time, 0, 1);
        assertParse("k", "1", time, 0, 1);
        assertParse("K", "1", time, 0, 1);

        cal = new GregorianCalendar(1970, Calendar.JANUARY, 1, 11, 0);
        time = cal.getTime();
        assertParse("h", "0011 ", time, 0, 4);
        assertParse("K", "11", time, 0, 2);
        cal = new GregorianCalendar(1970, Calendar.JANUARY, 1, 23, 0);
        time = cal.getTime();
        assertParse("H", "23", time, 0, 2);
        assertParse("k", "23", time, 0, 2);

        assertParse("h a", " 3 AM", new GregorianCalendar(1970,
                Calendar.JANUARY, 1, 3, 0).getTime(), 1, 5);
        assertParse("K a", " 3 pm ", new GregorianCalendar(1970,
                Calendar.JANUARY, 1, 15, 0).getTime(), 1, 5);
        assertParse("m:s", "0:59 ", new GregorianCalendar(1970,
                Calendar.JANUARY, 1, 0, 0, 59).getTime(), 0, 4);
        assertParse("m:s", "59:0", new GregorianCalendar(1970, Calendar.JANUARY,
                1, 0, 59, 0).getTime(), 0, 4);
        assertParse("ms", "059", new GregorianCalendar(1970, Calendar.JANUARY,
                1, 0, 0, 59).getTime(), 0, 3);

        cal = new GregorianCalendar(1970, Calendar.JANUARY, 1);
        assertParse("S", "0", cal.getTime(), 0, 1);
        cal.setTimeZone(TimeZone.getTimeZone("HST"));
        cal.set(Calendar.MILLISECOND, 999);
        assertParse("S z", "999 HST", cal.getTime(), 0, 7);

        cal = new GregorianCalendar(1970, Calendar.JANUARY, 1);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        assertParse("G", "Bc ", cal.getTime(), 0, 2);
    }

    public void test_parse_y() throws Exception {
        assertParse("y", "00", new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime(), 0, 2);
        assertParse("y", "99", new GregorianCalendar(1999, Calendar.JANUARY, 1).getTime(), 0, 2);
        assertParse("y", "1", new GregorianCalendar(1, Calendar.JANUARY, 1).getTime(), 0, 1);
        assertParse("y", "-1", new GregorianCalendar(-1, Calendar.JANUARY, 1).getTime(), 0, 2);
        assertParse("y", "001", new GregorianCalendar(1, Calendar.JANUARY, 1).getTime(), 0, 3);
        assertParse("y", "2005", new GregorianCalendar(2005, Calendar.JANUARY, 1).getTime(), 0, 4);
    }

    public void test_parse_yy() throws Exception {
        assertParse("yy", "00", new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime(), 0, 2);
        assertParse("yy", "99", new GregorianCalendar(1999, Calendar.JANUARY, 1).getTime(), 0, 2);
        assertParse("yy", "1", new GregorianCalendar(1, Calendar.JANUARY, 1).getTime(), 0, 1);
        assertParse("yy", "-1", new GregorianCalendar(-1, Calendar.JANUARY, 1).getTime(), 0, 2);
        assertParse("yy", "001", new GregorianCalendar(1, Calendar.JANUARY, 1).getTime(), 0, 3);
        assertParse("yy", "2005", new GregorianCalendar(2005, Calendar.JANUARY, 1).getTime(), 0, 4);
    }

    public void test_parse_yyy() throws Exception {
        assertParse("yyy", "99", new GregorianCalendar(99, Calendar.JANUARY, 1).getTime(), 0, 2);
        assertParse("yyy", "1", new GregorianCalendar(1, Calendar.JANUARY, 1).getTime(), 0, 1);
        assertParse("yyy", "-1", new GregorianCalendar(-1, Calendar.JANUARY, 1).getTime(), 0, 2);
        assertParse("yyy", "001", new GregorianCalendar(1, Calendar.JANUARY, 1).getTime(), 0, 3);
        assertParse("yyy", "2005", new GregorianCalendar(2005, Calendar.JANUARY, 1).getTime(),
                0, 4);
    }

    public void test_parse_yyyy() throws Exception {
        assertParse("yyyy", "99", new GregorianCalendar(99, Calendar.JANUARY, 1).getTime(), 0, 2);
        assertParse("yyyy", "  1999", new GregorianCalendar(1999, Calendar.JANUARY, 1).getTime(),
                2, 6);
    }

    public void test_parse_monthPatterns() throws Exception {
        assertParse("MM'M'", "4M", new GregorianCalendar(1970, Calendar.APRIL, 1).getTime(), 0, 2);
        assertParse("MMM", "Feb", new GregorianCalendar(1970, Calendar.FEBRUARY, 1).getTime(),
                0, 3);
        assertParse("MMMM d", "April 14 ",
                new GregorianCalendar(1970, Calendar.APRIL, 14).getTime(), 0, 8);
        assertParse("MMMMd", "April14 ", new GregorianCalendar(1970, Calendar.APRIL, 14).getTime(),
                0, 7);
        assertParse("E w", "Mon 12", new GregorianCalendar(1970, Calendar.MARCH, 16).getTime(),
                0, 6);
        assertParse("Ew", "Mon12", new GregorianCalendar(1970, Calendar.MARCH, 16).getTime(), 0, 5);
        assertParse("M EE ''W", "5 Tue '2", new GregorianCalendar(1970, Calendar.MAY, 5).getTime(),
                0, 8);
        assertParse("MEE''W", "5Tue'2", new GregorianCalendar(1970, Calendar.MAY, 5).getTime(),
                0, 6);
        assertParse("MMM EEE F", " JUL Sunday 3",
                new GregorianCalendar(1970, Calendar.JULY, 19).getTime(), 1, 13);
        assertParse("MMMEEEF", " JULSunday3",
                new GregorianCalendar(1970, Calendar.JULY, 19).getTime(), 1, 11);
    }

    public void test_parse_dayOfYearPatterns() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, Calendar.JANUARY, 1);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+0:1"));
        cal.set(Calendar.DAY_OF_YEAR, 243);
        assertParse("D z", "243 GMT+0:0", cal.getTime(), 0, 11);
    }

    public void test_parse_h_m_z() throws Exception {
        GregorianCalendar cal = new GregorianCalendar(1970, Calendar.JANUARY, 1);
        cal.setTimeZone(TimeZone.getTimeZone("EST"));
        cal.set(1970, Calendar.JANUARY, 1, 4, 30);
        assertParse("h:m z", "4:30 GMT-5 ", cal.getTime(), 0, 10);
    }

    public void test_parse_h_z_2DigitOffsetFromGMT() throws Exception {
        assertParse("h z", "14 GMT-24 ", new Date(51840000), 0, 9);
        assertParse("h z", "14 GMT-23 ", new Date(133200000), 0, 9);
        assertParse("h z", "14 GMT+24 ", new Date(48960000), 0, 9);
        assertParse("h z", "14 GMT+23 ", new Date(-32400000), 0, 9);
    }

    public void test_parse_h_z_4DigitOffsetFromGMT() throws Exception {
        assertParse("h z", "14 GMT-0100 ", new Date(54000000), 0, 11);
        assertParse("h z", "14 GMT+0100 ", new Date(46800000), 0, 11);
    }

    public void test_parse_h_z_4DigitOffsetNoGMT() throws Exception {
        assertParse("h z", "14 +0100 ", new Date(46800000), 0, 8);
        assertParse("h z", "14 -0100 ", new Date(54000000), 0, 8);
    }

    public void test_parse_yyyyMMddHHmmss() throws Exception {
        assertParse("yyyyMMddHHmmss", "19990913171901",
                new GregorianCalendar(1999, Calendar.SEPTEMBER, 13, 17, 19, 1).getTime(), 0, 14);
    }

    public void test_parse_dd_MMMM_yyyy_EEEE() throws Exception {
        checkPatternOnFixedDate("dd MMMM yyyy EEEE", "11 March 2002 Monday");
    }

    public void test_parse_dd_MMMM_yyyy_F() throws Exception {
        checkPatternOnFixedDate("dd MMMM yyyy F", "11 March 2002 2");
    }

    public void test_parse_dd_MMMM_yyyy_w() throws Exception {
        checkPatternOnFixedDate("dd MMMM yyyy w", "11 March 2002 11");
    }

    public void test_parse_dd_MMMM_yyyy_W() throws Exception {
        checkPatternOnFixedDate("dd MMMM yyyy W", "11 March 2002 3");
    }

    public void test_parse_dd_MMMM_yyyy_D() throws Exception {
        // The day of the year overrides the day of the month.
        checkPatternOnFixedDate("dd MMMM yyyy D", "11 March 2002 70", "5 January 2002 70");
    }

    public void test_parse_W_w_dd_MMMM_yyyy_EEEE() throws Exception {
        checkPatternOnFixedDate("W w dd MMMM yyyy EEEE", "3 11 11 March 2002 Monday",
                "3 12 5 March 2002 Monday");
    }

    public void test_parse_w_W_dd_MMMM_yyyy_EEEE() throws Exception {
        checkPatternOnFixedDate("w W dd MMMM yyyy EEEE", "11 3 11 March 2002 Monday",
                "12 3 5 March 2002 Monday");
    }

    public void test_parse_F_dd_MMMM_yyyy_EEEE() throws Exception {
        checkPatternOnFixedDate("F dd MMMM yyyy EEEE", "2 11 March 2002 Monday",
                "2 5 March 2002 Monday");
    }

    public void test_parse_w_dd_MMMM_yyyy_EEEE() throws Exception {
        checkPatternOnFixedDate("w dd MMMM yyyy EEEE", "11 11 March 2002 Monday",
                "11 5 January 2002 Monday");
    }

    public void test_parse_w_dd_yyyy_EEEE_MMMM() throws Exception {
        checkPatternOnFixedDate("w dd yyyy EEEE MMMM", "11 11 2002 Monday March",
                "11 5 2002 Monday January");
    }

    public void test_parse_w_yyyy_EEEE_MMMM_dd() throws Exception {
        checkPatternOnFixedDate("w yyyy EEEE MMMM dd", "11 2002 Monday March 11",
                "17 2002 Monday March 11");
    }

    public void test_parse_dd_D_yyyy_MMMM() throws Exception {
        checkPatternOnFixedDate("dd D yyyy MMMM", "11 70 2002 March", "5 70 2002 January");
    }

    public void test_parse_D_dd_yyyy_MMMM() throws Exception {
        checkPatternOnFixedDate("D dd yyyy MMMM", "70 11 2002 March", "240 11 2002 March");
    }

    private static void checkPatternOnFixedDate(String pattern, String expectedOutput)
            throws ParseException {
        checkPatternOnFixedDate(pattern, expectedOutput, expectedOutput);
    }

    private static void checkPatternOnFixedDate(String pattern, String expectedOutput, String input)
            throws ParseException {
        Date d = new Date(1015822800000L);
        SimpleDateFormat df = new SimpleDateFormat("", new Locale("en", "US"));
        df.setTimeZone(TimeZone.getTimeZone("EST"));

        df.applyPattern(pattern);

        String output = df.format(d);
        assertEquals("Invalid output '" + pattern + "'", expectedOutput, output);
        Date date = df.parse(input);
        assertEquals("Invalid result '" + pattern + "'", d, date);
    }

    public void test_parse_nullParsePosition() {
        try {
            format.parse("240 11 2002 March", null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_parse_nullInput() {
        try {
            format.parse(null, new ParsePosition(0));
            fail();
        } catch (NullPointerException expected) {
        }
    }

    private void assertParse(String pattern, String input, Date expected, int start, int end) {
        pFormat.applyPattern(pattern);
        ParsePosition position = new ParsePosition(start);
        Date result = pFormat.parse(input, position);
        assertTrue("Wrong result: " + pattern + " input: " + input + " expected: " + expected +
                " result: " + result, expected.equals(result));
        assertTrue("Wrong end position: " + pattern + " input: " + input,
                position.getIndex() == end);
    }

    public void test_set2DigitYearStartLjava_util_Date() {
        // Test for method void
        // java.text.SimpleDateFormat.set2DigitYearStart(java.util.Date)
        SimpleDateFormat f1 = new SimpleDateFormat("yy");
        f1.set2DigitYearStart(new GregorianCalendar(1950, Calendar.JANUARY, 1).getTime());
        Calendar cal = new GregorianCalendar();
        try {
            cal.setTime(f1.parse("49"));
            assertEquals("Incorrect year 2049", 2049, cal.get(Calendar.YEAR));
            cal.setTime(f1.parse("50"));
            int year = cal.get(Calendar.YEAR);
            assertTrue("Incorrect year 1950: " + year, year == 1950);
            f1.applyPattern("y");
            cal.setTime(f1.parse("00"));
            assertEquals("Incorrect year 2000", 2000, cal.get(Calendar.YEAR));
            f1.applyPattern("yyy");
            cal.setTime(f1.parse("50"));
            assertEquals("Incorrect year 50", 50, cal.get(Calendar.YEAR));
        } catch (ParseException e) {
            fail("ParseException");
        }
    }

    public void test_setDateFormatSymbolsLjava_text_DateFormatSymbols() {
        // Test for method void
        // java.text.SimpleDateFormat.setDateFormatSymbols(java.text.DateFormatSymbols)
        SimpleDateFormat f1 = new SimpleDateFormat("a");
        DateFormatSymbols symbols = new DateFormatSymbols();
        symbols.setAmPmStrings(new String[] { "morning", "night" });
        f1.setDateFormatSymbols(symbols);
        DateFormatSymbols newSym = f1.getDateFormatSymbols();
        assertTrue("Set incorrectly", newSym.equals(symbols));
        assertTrue("Not a clone", f1.getDateFormatSymbols() != symbols);
        String result = f1.format(new GregorianCalendar(1999, Calendar.JUNE, 12, 3, 0).getTime());
        assertEquals("Incorrect symbols used", "morning", result);
        symbols.setEras(new String[] { "before", "after" });
        assertTrue("Identical symbols", !f1.getDateFormatSymbols().equals(symbols));

        try {
            f1.setDateFormatSymbols(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_toPattern() {
        String pattern = "yyyy mm dd";
        SimpleDateFormat f = new SimpleDateFormat(pattern);
        assertEquals("Wrong pattern: " + pattern, pattern, f.toPattern());

        pattern = "GyMdkHmsSEDFwWahKz";
        f = new SimpleDateFormat("GyMdkHmsSEDFwWahKz", new Locale("de", "CH"));
        assertTrue("Wrong pattern: " + pattern, f.toPattern().equals(pattern));

        pattern = "G y M d Z";
        f = new SimpleDateFormat(pattern, new Locale("de", "CH"));
        pattern = f.toPattern();
        assertTrue("Wrong pattern: " + pattern, f.toPattern().equals(pattern));
    }

    public void test_toLocalizedPattern() {
        SimpleDateFormat f2 = new SimpleDateFormat("GyMdkHmsSEDFwWahKzZLc", new Locale("de", "CH"));
        assertEquals(f2.toPattern(), f2.toLocalizedPattern());
    }

    public void test_parse_with_spaces() {
        // Regression for HARMONY-502
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setLenient(false);

        char allowed_chars[] = { 0x9, 0x20 };
        String allowed_char_names[] = { "tab", "space" };
        for (int i = 0; i < allowed_chars.length; i++) {
            Date expected = new GregorianCalendar(1970, Calendar.JANUARY, 1, 9, 7, 6).getTime();
            ParsePosition pp = new ParsePosition(0);
            Date d = df.parse(allowed_chars[i] + "9:07:06", pp);
            assertNotNull("hour may be prefixed by " + allowed_char_names[i], d);
            assertEquals(expected, d);

            pp = new ParsePosition(0);
            d = df.parse("09:" + allowed_chars[i] + "7:06", pp);
            assertNotNull("minute may be prefixed by " + allowed_char_names[i], d);
            assertEquals(expected, d);

            pp = new ParsePosition(0);
            d = df.parse("09:07:" + allowed_chars[i] + "6", pp);
            assertNotNull("second may be prefixed by " + allowed_char_names[i], d);
            assertEquals(expected, d);
        }

        char not_allowed_chars[] = {
                // whitespace
                0x1c, 0x1d, 0x1e, 0x1f, 0xa, 0xb, 0xc, 0xd, 0x2001, 0x2002,
                0x2003, 0x2004, 0x2005, 0x2006, 0x2008, 0x2009, 0x200a, 0x200b,
                0x2028, 0x2029, 0x3000,
                // non-breaking space
                0xA0, 0x2007, 0x202F };

        for (int i = 0; i < not_allowed_chars.length; i++) {
            ParsePosition pp = new ParsePosition(0);
            Date d = df.parse(not_allowed_chars[i] + "9:07", pp);
            assertNull(d);

            pp = new ParsePosition(0);
            d = df.parse("09:" + not_allowed_chars[i] + "7", pp);
            assertNull(d);

            pp = new ParsePosition(0);
            d = df.parse("09:07:" + not_allowed_chars[i] + "6", pp);
            assertNull(d);
        }
    }
}
