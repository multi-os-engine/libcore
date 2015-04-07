/*
 * Copyright (C) 2013 The Android Open Source Project
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

import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import libcore.util.BasicLruCache;

/**
 * Exposes icu4j's DateIntervalFormat.
 */
public final class DateIntervalFormat {

  // These are all public API in DateUtils. There are others, but they're either for use with
  // other methods (like FORMAT_ABBREV_RELATIVE), don't internationalize (like FORMAT_CAP_AMPM),
  // or have never been implemented anyway.
  public static final int FORMAT_SHOW_TIME      = 0x00001;
  public static final int FORMAT_SHOW_WEEKDAY   = 0x00002;
  public static final int FORMAT_SHOW_YEAR      = 0x00004;
  public static final int FORMAT_NO_YEAR        = 0x00008;
  public static final int FORMAT_SHOW_DATE      = 0x00010;
  public static final int FORMAT_NO_MONTH_DAY   = 0x00020;
  public static final int FORMAT_12HOUR         = 0x00040;
  public static final int FORMAT_24HOUR         = 0x00080;
  public static final int FORMAT_UTC            = 0x02000;
  public static final int FORMAT_ABBREV_TIME    = 0x04000;
  public static final int FORMAT_ABBREV_WEEKDAY = 0x08000;
  public static final int FORMAT_ABBREV_MONTH   = 0x10000;
  public static final int FORMAT_NUMERIC_DATE   = 0x20000;
  public static final int FORMAT_ABBREV_ALL     = 0x80000;

  private static final int DAY_IN_MS = 24 * 60 * 60 * 1000;
  private static final int EPOCH_JULIAN_DAY = 2440588;

  private static final FormatterCache CACHED_FORMATTERS = new FormatterCache();

  static class FormatterCache extends BasicLruCache<String, com.ibm.icu.text.DateIntervalFormat> {
    FormatterCache() {
      super(8);
    }
  }

  private DateIntervalFormat() {
  }

  // This is public DateUtils API in frameworks/base.
  public static String formatDateRange(long startMs, long endMs, int flags, String olsonId) {
    if ((flags & FORMAT_UTC) != 0) {
      olsonId = "UTC";
    }
    TimeZone tz = (olsonId != null) ? TimeZone.getTimeZone(olsonId) : TimeZone.getDefault();
    return formatDateRange(Locale.getDefault(), tz, startMs, endMs, flags);
  }

  // This is our slightly more sensible internal API. (A truly sane replacement would take a
  // skeleton instead of int flags.)
  public static String formatDateRange(Locale locale, TimeZone tz, long startMs, long endMs, int flags) {
    Calendar startCalendar = Calendar.getInstance(tz);
    startCalendar.setTimeInMillis(startMs);

    Calendar endCalendar;
    if (startMs == endMs) {
      endCalendar = startCalendar;
    } else {
      endCalendar = Calendar.getInstance(tz);
      endCalendar.setTimeInMillis(endMs);
    }

    boolean endsAtMidnight = isMidnight(endCalendar);

    // If we're not showing the time or the start and end times are on the same day, and the
    // end time is midnight, fudge the end date so we don't count the day that's about to start.
    // This is not the behavior of icu4j's DateIntervalFormat, but it's the historical behavior
    // of Android's DateUtils.formatDateRange.
    if (startMs != endMs && endsAtMidnight &&
        ((flags & FORMAT_SHOW_TIME) == 0 || dayDistance(startCalendar, endCalendar) <= 1)) {
      endCalendar.roll(Calendar.DAY_OF_MONTH, false);
      endMs -= DAY_IN_MS;
    }

    String skeleton = toSkeleton(startCalendar, endCalendar, flags);
    synchronized (CACHED_FORMATTERS) {
      com.ibm.icu.text.DateIntervalFormat fmtter = getFormatter(skeleton, locale, tz);
      com.ibm.icu.util.Calendar scal = icuCalendar(startCalendar);
      com.ibm.icu.util.Calendar ecal = icuCalendar(endCalendar);
      String result = fmtter.format(scal, ecal, new StringBuffer(), new FieldPosition(0)).toString();
      return result;
    }
  }

  private static com.ibm.icu.text.DateIntervalFormat getFormatter(String skeleton, Locale locale, TimeZone tz) {
    String key = skeleton + "\t" + locale + "\t" + tz;
    com.ibm.icu.text.DateIntervalFormat formatter = CACHED_FORMATTERS.get(key);
    if (formatter != null) {
      return formatter;
    }
    formatter = com.ibm.icu.text.DateIntervalFormat.getInstance(skeleton, locale);
    formatter.setTimeZone(icuTimeZone(tz));
    CACHED_FORMATTERS.put(key, formatter);
    return formatter;
  }

  private static String toSkeleton(Calendar startCalendar, Calendar endCalendar, int flags) {
    if ((flags & FORMAT_ABBREV_ALL) != 0) {
      flags |= FORMAT_ABBREV_MONTH | FORMAT_ABBREV_TIME | FORMAT_ABBREV_WEEKDAY;
    }

    String monthPart = "MMMM";
    if ((flags & FORMAT_NUMERIC_DATE) != 0) {
      monthPart = "M";
    } else if ((flags & FORMAT_ABBREV_MONTH) != 0) {
      monthPart = "MMM";
    }

    String weekPart = "EEEE";
    if ((flags & FORMAT_ABBREV_WEEKDAY) != 0) {
      weekPart = "EEE";
    }

    String timePart = "j"; // "j" means choose 12 or 24 hour based on current locale.
    if ((flags & FORMAT_24HOUR) != 0) {
      timePart = "H";
    } else if ((flags & FORMAT_12HOUR) != 0) {
      timePart = "h";
    }

    // If we've not been asked to abbreviate times, or we're using the 24-hour clock (where it
    // never makes sense to leave out the minutes), include minutes. This gets us times like
    // "4 PM" while avoiding times like "16" (for "16:00").
    if ((flags & FORMAT_ABBREV_TIME) == 0 || (flags & FORMAT_24HOUR) != 0) {
      timePart += "m";
    } else {
      // Otherwise, we're abbreviating a 12-hour time, and should only show the minutes
      // if they're not both "00".
      if (!(onTheHour(startCalendar) && onTheHour(endCalendar))) {
        timePart = timePart + "m";
      }
    }

    if (fallOnDifferentDates(startCalendar, endCalendar)) {
      flags |= FORMAT_SHOW_DATE;
    }

    if (fallInSameMonth(startCalendar, endCalendar) && (flags & FORMAT_NO_MONTH_DAY) != 0) {
      flags &= (~FORMAT_SHOW_WEEKDAY);
      flags &= (~FORMAT_SHOW_TIME);
    }

    if ((flags & (FORMAT_SHOW_DATE | FORMAT_SHOW_TIME | FORMAT_SHOW_WEEKDAY)) == 0) {
      flags |= FORMAT_SHOW_DATE;
    }

    // If we've been asked to show the date, work out whether we think we should show the year.
    if ((flags & FORMAT_SHOW_DATE) != 0) {
      if ((flags & FORMAT_SHOW_YEAR) != 0) {
        // The caller explicitly wants us to show the year.
      } else if ((flags & FORMAT_NO_YEAR) != 0) {
        // The caller explicitly doesn't want us to show the year, even if we otherwise would.
      } else if (!fallInSameYear(startCalendar, endCalendar) || !isThisYear(startCalendar)) {
        flags |= FORMAT_SHOW_YEAR;
      }
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

  private static boolean isMidnight(Calendar c) {
    return c.get(Calendar.HOUR_OF_DAY) == 0 &&
        c.get(Calendar.MINUTE) == 0 &&
        c.get(Calendar.SECOND) == 0 &&
        c.get(Calendar.MILLISECOND) == 0;
  }

  private static boolean onTheHour(Calendar c) {
    return c.get(Calendar.MINUTE) == 0 && c.get(Calendar.SECOND) == 0;
  }

  private static boolean fallOnDifferentDates(Calendar c1, Calendar c2) {
    return c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR) ||
        c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH) ||
        c1.get(Calendar.DAY_OF_MONTH) != c2.get(Calendar.DAY_OF_MONTH);
  }

  private static boolean fallInSameMonth(Calendar c1, Calendar c2) {
    return c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
  }

  private static boolean fallInSameYear(Calendar c1, Calendar c2) {
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
  }

  private static boolean isThisYear(Calendar c) {
    Calendar now = Calendar.getInstance(c.getTimeZone());
    return c.get(Calendar.YEAR) == now.get(Calendar.YEAR);
  }

  // Return the date difference for the two times in a given timezone.
  public static int dayDistance(TimeZone tz, long startTime, long endTime) {
    return julianDay(tz, endTime) - julianDay(tz, startTime);
  }

  public static int dayDistance(Calendar c1, Calendar c2) {
    return julianDay(c2) - julianDay(c1);
  }

  private static int julianDay(TimeZone tz, long time) {
    long utcMs = time + tz.getOffset(time);
    return (int) (utcMs / DAY_IN_MS) + EPOCH_JULIAN_DAY;
  }

  private static int julianDay(Calendar c) {
    long utcMs = c.getTimeInMillis() + c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET);
    return (int) (utcMs / DAY_IN_MS) + EPOCH_JULIAN_DAY;
  }

  private static com.ibm.icu.util.TimeZone icuTimeZone(TimeZone tz) {
    return com.ibm.icu.util.TimeZone.getTimeZone(tz.getID(), com.ibm.icu.util.TimeZone.TIMEZONE_JDK);
  }

  private static com.ibm.icu.util.Calendar icuCalendar(Calendar cal) {
    com.ibm.icu.util.Calendar result = com.ibm.icu.util.Calendar.getInstance(icuTimeZone(cal.getTimeZone()));
    result.setTime(cal.getTime());
    return result;
  }

}
