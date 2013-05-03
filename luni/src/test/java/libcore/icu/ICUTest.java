/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.util.Arrays;
import java.util.Locale;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ICUTest extends junit.framework.TestCase {
  public void test_getISOLanguages() throws Exception {
    // Check that corrupting our array doesn't affect other callers.
    assertNotNull(ICU.getISOLanguages()[0]);
    ICU.getISOLanguages()[0] = null;
    assertNotNull(ICU.getISOLanguages()[0]);
  }

  public void test_getISOCountries() throws Exception {
    // Check that corrupting our array doesn't affect other callers.
    assertNotNull(ICU.getISOCountries()[0]);
    ICU.getISOCountries()[0] = null;
    assertNotNull(ICU.getISOCountries()[0]);
  }

  public void test_getAvailableLocales() throws Exception {
    // Check that corrupting our array doesn't affect other callers.
    assertNotNull(ICU.getAvailableLocales()[0]);
    ICU.getAvailableLocales()[0] = null;
    assertNotNull(ICU.getAvailableLocales()[0]);
  }

  public void test_getBestDateTimePattern() throws Exception {
    assertEquals("d MMMM", ICU.getBestDateTimePattern("MMMMd", "ca_ES"));
    assertEquals("d 'de' MMMM", ICU.getBestDateTimePattern("MMMMd", "es_ES"));
    assertEquals("d. MMMM", ICU.getBestDateTimePattern("MMMMd", "de_CH"));
    assertEquals("MMMM d", ICU.getBestDateTimePattern("MMMMd", "en_US"));
    assertEquals("d LLLL", ICU.getBestDateTimePattern("MMMMd", "fa_IR"));
    assertEquals("M月d日", ICU.getBestDateTimePattern("MMMMd", "ja_JP"));
  }

  public void test_localeFromString() throws Exception {
    // localeFromString is pretty lenient. Some of these can't be round-tripped
    // through Locale.toString.
    assertEquals(Locale.ENGLISH, ICU.localeFromString("en"));
    assertEquals(Locale.ENGLISH, ICU.localeFromString("en_"));
    assertEquals(Locale.ENGLISH, ICU.localeFromString("en__"));
    assertEquals(Locale.US, ICU.localeFromString("en_US"));
    assertEquals(Locale.US, ICU.localeFromString("en_US_"));
    assertEquals(new Locale("", "US", ""), ICU.localeFromString("_US"));
    assertEquals(new Locale("", "US", ""), ICU.localeFromString("_US_"));
    assertEquals(new Locale("", "", "POSIX"), ICU.localeFromString("__POSIX"));
    assertEquals(new Locale("aa", "BB", "CC"), ICU.localeFromString("aa_BB_CC"));
  }

  public void test_getScript_addLikelySubtags() throws Exception {
    assertEquals("Latn", ICU.getScript(ICU.addLikelySubtags("en_US")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("he")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("he_IL")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("iw")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("iw_IL")));
  }

  private String best(Locale l, String skeleton) {
    return ICU.getBestDateTimePattern(skeleton, l.toString());
  }

  public void test_getDateFormatOrder() throws Exception {
    // lv and fa use differing orders depending on whether you're using numeric or textual months.
    Locale lv = new Locale("lv");
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(lv, "yyyy-M-dd"))));
    assertEquals("[y, d, M]", Arrays.toString(ICU.getDateFormatOrder(best(lv, "yyyy-MMM-dd"))));
    assertEquals("[d, M, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(lv, "MMM-dd"))));
    Locale fa = new Locale("fa");
    assertEquals("[y, M, d]", Arrays.toString(ICU.getDateFormatOrder(best(fa, "yyyy-M-dd"))));
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(fa, "yyyy-MMM-dd"))));
    assertEquals("[d, M, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(fa, "MMM-dd"))));

    // English differs on each side of the Atlantic.
    Locale en_US = Locale.US;
    assertEquals("[M, d, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_US, "yyyy-M-dd"))));
    assertEquals("[M, d, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_US, "yyyy-MMM-dd"))));
    assertEquals("[M, d, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(en_US, "MMM-dd"))));
    Locale en_GB = Locale.UK;
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_GB, "yyyy-M-dd"))));
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_GB, "yyyy-MMM-dd"))));
    assertEquals("[d, M, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(en_GB, "MMM-dd"))));

    assertEquals("[y, M, d]", Arrays.toString(ICU.getDateFormatOrder("yyyy - 'why' '' 'ddd' MMM-dd")));

    try {
      ICU.getDateFormatOrder("the quick brown fox jumped over the lazy dog");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      ICU.getDateFormatOrder("'");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      ICU.getDateFormatOrder("yyyy'");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      ICU.getDateFormatOrder("yyyy'MMM");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  // TODO: this should be our API.
  static class DateUtils {
    public static final int FORMAT_SHOW_TIME = 0x00001;
    public static final int FORMAT_SHOW_WEEKDAY = 0x00002;
    public static final int FORMAT_SHOW_YEAR = 0x00004;
    public static final int FORMAT_NO_YEAR = 0x00008;
    public static final int FORMAT_SHOW_DATE = 0x00010;
    public static final int FORMAT_NO_MONTH_DAY = 0x00020;
    public static final int FORMAT_12HOUR = 0x00040;
    public static final int FORMAT_24HOUR = 0x00080;
    public static final int FORMAT_CAP_AMPM = 0x00100;
    public static final int FORMAT_NO_NOON = 0x00200;
    public static final int FORMAT_CAP_NOON = 0x00400;
    public static final int FORMAT_NO_MIDNIGHT = 0x00800;
    public static final int FORMAT_CAP_MIDNIGHT = 0x01000;
    public static final int FORMAT_UTC = 0x02000;
    public static final int FORMAT_ABBREV_TIME = 0x04000;
    public static final int FORMAT_ABBREV_WEEKDAY = 0x08000;
    public static final int FORMAT_ABBREV_MONTH = 0x10000;
    public static final int FORMAT_NUMERIC_DATE = 0x20000;
    public static final int FORMAT_ABBREV_RELATIVE = 0x40000;
    public static final int FORMAT_ABBREV_ALL = 0x80000;
    public static final int FORMAT_CAP_NOON_MIDNIGHT = (FORMAT_CAP_NOON | FORMAT_CAP_MIDNIGHT);
    public static final int FORMAT_NO_NOON_MIDNIGHT = (FORMAT_NO_NOON | FORMAT_NO_MIDNIGHT);

    public static String formatDateRange(long startMillis, long endMillis, int flags) {
      return formatDateRange(startMillis, endMillis, flags, null);
    }

    public static String formatDateRange(long startMillis, long endMillis, int flags, String timeZone) {
      String skeleton = toSkeleton(startMillis, endMillis, flags, timeZone);
      System.err.println(" skeleton=" + skeleton);
      String result = ICU.formatDateInterval(skeleton, "en_US", startMillis, endMillis);
      //System.err.println(" result=" + result);
      return result;
    }

    private static Calendar makeCalendar(String timeZone, long t) {
      Calendar c;
      if (timeZone != null) {
        c = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
      } else {
        c = Calendar.getInstance();
      }
      c.setTimeInMillis(t);
      return c;
    }

    private static String toSkeleton(long startMillis, long endMillis, int flags, String timeZone) {
      if ((flags & FORMAT_UTC) != 0) {
        timeZone = "UTC";
      }

      Calendar startCalendar = makeCalendar(timeZone, startMillis);
      Calendar endCalendar = makeCalendar(timeZone, endMillis);

      String monthPart = "MMMM";
      if ((flags & FORMAT_NUMERIC_DATE) != 0) {
        monthPart = "M";
      } else if ((flags & (FORMAT_ABBREV_MONTH | FORMAT_ABBREV_ALL)) != 0) {
        monthPart = "MMM";
      }

      String weekPart = "EEEE";
      if ((flags & (FORMAT_ABBREV_WEEKDAY | FORMAT_ABBREV_ALL)) != 0) {
        weekPart = "EEE";
      }

      String timePart = "j"; // "j" means choose 12 or 24 hour based on current locale.
      if ((flags & FORMAT_24HOUR) != 0) {
        timePart = "H";
      } else if ((flags & FORMAT_12HOUR) != 0) {
        timePart = "h";
      }
      if ((flags & (FORMAT_ABBREV_TIME | FORMAT_ABBREV_ALL)) == 0 || !onTheHour(startCalendar) || !onTheHour(endCalendar)) {
        timePart = timePart + "m";
      }

      if (fallOnDifferentDates(startCalendar, endCalendar)) {
        flags |= FORMAT_SHOW_DATE;
      }

      if (fallInSameMonth(startCalendar, endCalendar)  && (flags & FORMAT_NO_MONTH_DAY) != 0) {
        flags &= (~FORMAT_SHOW_WEEKDAY);
        flags &= (~FORMAT_SHOW_TIME);
      }

      if ((flags & (FORMAT_SHOW_DATE | FORMAT_SHOW_TIME | FORMAT_SHOW_WEEKDAY)) == 0) {
        flags |= FORMAT_SHOW_DATE;
      }

      StringBuilder builder = new StringBuilder();
      if ((flags & (FORMAT_SHOW_DATE | FORMAT_NO_MONTH_DAY)) != 0) {
        if ((flags & FORMAT_SHOW_YEAR) != 0) {
          builder.append("y");
        }
        builder.append(monthPart);
        if ((flags & FORMAT_NO_MONTH_DAY) == 0) {
          builder.append("d");
        }
      }
      if ((flags & FORMAT_SHOW_WEEKDAY) != 0) {
        builder.append(weekPart);
      }
      if ((flags & FORMAT_SHOW_TIME) != 0) {
        builder.append(timePart);
      }
      return builder.toString();
    }

    private static boolean onTheHour(Calendar c) {
      return c.get(Calendar.MINUTE) == 0 && c.get(Calendar.SECOND) == 0;
    }

    private static boolean fallOnDifferentDates(Calendar c1, Calendar c2) {
      return c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR) || c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH) || c1.get(Calendar.DAY_OF_MONTH) != c2.get(Calendar.DAY_OF_MONTH);
    }

    private static boolean fallInSameMonth(Calendar c1, Calendar c2) {
      return c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
    }
  }

  public static void assertEquals(String lhs, String rhs) {
    if (lhs.equals(rhs)) {
      System.err.println("PASS: " + lhs + " --- " + rhs);
    } else {
      System.err.println("FAIL: " + lhs + " --- " + rhs);
    }
  }

  public void test_formatDateInterval() throws Exception {
    Date date = new Date(109, 0, 19, 3, 30, 15);
    long fixedTime = date.getTime();

    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    Date dateWithCurrentYear = new Date(currentYear - 1900, 0, 19, 3, 30, 15);
    long timeWithCurrentYear = dateWithCurrentYear.getTime();

    long noonDuration = (8 * 60 + 30) * 60 * 1000 - 15 * 1000;
    long midnightDuration = (3 * 60 + 30) * 60 * 1000 + 15 * 1000;
    long integralDuration = 30 * 60 * 1000 + 15 * 1000;

    final long DAY_DURATION = 5 * 24 * 60 * 60 * 1000;
    final long HOUR_DURATION = 2 * 60 * 60 * 1000;

    // TODO: these are the current CTS tests for DateUtils.formatDateRange. note that not one of them tests an actual range!

    assertEquals("Monday", DateUtils.formatDateRange(fixedTime, fixedTime + HOUR_DURATION, DateUtils.FORMAT_SHOW_WEEKDAY));
    assertEquals("January 19", DateUtils.formatDateRange(timeWithCurrentYear, timeWithCurrentYear + HOUR_DURATION, DateUtils.FORMAT_SHOW_DATE));
    assertEquals("3:30AM", DateUtils.formatDateRange(fixedTime, fixedTime, DateUtils.FORMAT_SHOW_TIME));
    assertEquals("January 19, 2009", DateUtils.formatDateRange(fixedTime, fixedTime + HOUR_DURATION, DateUtils.FORMAT_SHOW_YEAR));
    assertEquals("January 19", DateUtils.formatDateRange(timeWithCurrentYear, timeWithCurrentYear + HOUR_DURATION, DateUtils.FORMAT_NO_YEAR));
    assertEquals("January", DateUtils.formatDateRange(timeWithCurrentYear, timeWithCurrentYear + HOUR_DURATION, DateUtils.FORMAT_NO_MONTH_DAY));
    assertEquals("3:30AM", DateUtils.formatDateRange(fixedTime, fixedTime, DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_SHOW_TIME));
    assertEquals("03:30", DateUtils.formatDateRange(fixedTime, fixedTime, DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_SHOW_TIME));
    assertEquals("3:30AM", DateUtils.formatDateRange(fixedTime, fixedTime, DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_CAP_AMPM | DateUtils.FORMAT_SHOW_TIME));
    assertEquals("noon", DateUtils.formatDateRange(fixedTime + noonDuration, fixedTime + noonDuration, DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_SHOW_TIME));
    assertEquals("Noon", DateUtils.formatDateRange(fixedTime + noonDuration, fixedTime + noonDuration, DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_CAP_NOON));
    assertEquals("12:00PM", DateUtils.formatDateRange(fixedTime + noonDuration, fixedTime + noonDuration, DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_NO_NOON | DateUtils.FORMAT_SHOW_TIME));
    assertEquals("12:00AM", DateUtils.formatDateRange(fixedTime - midnightDuration, fixedTime - midnightDuration, DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_MIDNIGHT));
    assertEquals("3:30AM", DateUtils.formatDateRange(fixedTime, fixedTime, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_UTC));
    assertEquals("3am", DateUtils.formatDateRange(fixedTime - integralDuration, fixedTime - integralDuration, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_TIME));
    assertEquals("Mon", DateUtils.formatDateRange(fixedTime, fixedTime + HOUR_DURATION, DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY));
    assertEquals("Jan 19", DateUtils.formatDateRange(timeWithCurrentYear, timeWithCurrentYear + HOUR_DURATION, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
    assertEquals("Jan 19", DateUtils.formatDateRange(timeWithCurrentYear, timeWithCurrentYear + HOUR_DURATION, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL));

    String actual = DateUtils.formatDateRange(fixedTime, fixedTime + HOUR_DURATION, DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
    // accept with leading zero or without
    assertTrue("1/19/2009".equals(actual) || "01/19/2009".equals(actual));
  }
}
