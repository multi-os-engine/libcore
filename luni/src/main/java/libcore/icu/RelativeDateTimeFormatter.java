/*
 * Copyright (C) 2015 The Android Open Source Project
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

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import libcore.util.BasicLruCache;
import libcore.icu.DateIntervalFormat;

/**
 * Exposes icu4c's RelativeDateTimeFormatter.
 */
public final class RelativeDateTimeFormatter {

  // Values from public API in DateUtils to be used in this class.
  public static final int FORMAT_SHOW_TIME = 0x00001;
  public static final int FORMAT_SHOW_YEAR = 0x00004;
  public static final int FORMAT_SHOW_DATE = 0x00010;
  public static final int FORMAT_ABBREV_MONTH = 0x10000;
  public static final int FORMAT_NUMERIC_DATE = 0x20000;
  public static final int FORMAT_ABBREV_RELATIVE = 0x40000;
  public static final int FORMAT_ABBREV_ALL = 0x80000;

  public static final long SECOND_IN_MILLIS = 1000;
  public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
  public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
  public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
  public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
  // 364 days in a year.
  public static final long YEAR_IN_MILLIS = WEEK_IN_MILLIS * 52;

  // Values from icu4c UDateRelativeUnit enum.
  public static final int UDAT_RELATIVE_SECONDS = 0;
  public static final int UDAT_RELATIVE_MINUTES = 1;
  public static final int UDAT_RELATIVE_HOURS = 2;
  public static final int UDAT_RELATIVE_DAYS = 3;
  public static final int UDAT_RELATIVE_WEEKS = 4;
  public static final int UDAT_RELATIVE_MONTHS = 5;
  public static final int UDAT_RELATIVE_YEARS = 6;

  // Values from icu4c UdateAbsoluteUnit enum.
  public static final int UDAT_ABSOLUTE_DAY = 7;

  // Values from icu4c UDateDirection enum.
  public static final int UDAT_DIRECTION_LAST_2 = 0;
  public static final int UDAT_DIRECTION_LAST = 1;
  public static final int UDAT_DIRECTION_THIS = 2;
  public static final int UDAT_DIRECTION_NEXT = 3;
  public static final int UDAT_DIRECTION_NEXT_2 = 4;
  public static final int UDAT_DIRECTION_PLAIN = 5;

  // Values from icu4c UDateRelativeDateTimeFormatterStyle.
  public static final int UDAT_STYLE_LONG = 0;
  public static final int UDAT_STYLE_SHORT = 1;
  public static final int UDAT_STYLE_NARROW = 2;

  private static final FormatterCache CACHED_FORMATTERS = new FormatterCache();
  private static final int EPOCH_JULIAN_DAY = 2440588;

  static class FormatterCache extends BasicLruCache<String, Long> {
    FormatterCache() {
      super(8);
    }

    protected void entryEvicted(String key, Long value) {
      destroyRelativeDateTimeFormatter(value);
    }
  };

  private RelativeDateTimeFormatter() {
  }

  // This is public DateUtils API in frameworks/base.
  public static String getRelativeTimeSpanString(long time, long now, long minResolution,
      int flags) {
    return getRelativeTimeSpanString(Locale.getDefault(), TimeZone.getDefault(), time, now,
                                     minResolution, flags);
  }

  // Internal API that accepts locale and timezone
  public static String getRelativeTimeSpanString(Locale locale, TimeZone tz, long time, long now,
      long minResolution, int flags) {
    if (locale == null) {
      locale = Locale.getDefault();
    }
    if (tz == null) {
      tz = TimeZone.getDefault();
    }
    long duration = Math.abs(now - time);
    boolean past = (now >= time);

    // Use UDAT_STYLE_SHORT or UDAT_STYLE_LONG.
    int style;
    if ((flags & (FORMAT_ABBREV_RELATIVE | FORMAT_ABBREV_ALL)) != 0) {
        style = UDAT_STYLE_SHORT;
    } else {
        style = UDAT_STYLE_LONG;
    }

    // Use UDAT_DIRECTION_LAST or UDAT_DIRECTION_NEXT.
    int direction;
    if (past) {
        direction = UDAT_DIRECTION_LAST;
    } else {
        direction = UDAT_DIRECTION_NEXT;
    }

    long address = 0;
    // 'relative' defaults to true as we are generating relative time span string. It will be set
    // to false when we try to display strings without a quantity, such as 'yesterday', etc.
    boolean relative = true;
    int count;
    int unit;

    if (duration < MINUTE_IN_MILLIS && minResolution < MINUTE_IN_MILLIS) {
      count = (int)(duration / SECOND_IN_MILLIS);
      unit = UDAT_RELATIVE_SECONDS;
    } else if (duration < HOUR_IN_MILLIS && minResolution < HOUR_IN_MILLIS) {
      count = (int)(duration / MINUTE_IN_MILLIS);
      unit = UDAT_RELATIVE_MINUTES;
    } else if (duration < DAY_IN_MILLIS && minResolution < DAY_IN_MILLIS) {
      // Even if 'time' actually happened yesterday, we don't format it as
      // "yesterday" in this case. Unless the duration is longer than a day,
      // or minResolution is specified as DAY_IN_MILLIS by user.
      count = (int)(duration / HOUR_IN_MILLIS);
      unit = UDAT_RELATIVE_HOURS;
    } else if (duration < WEEK_IN_MILLIS && minResolution < WEEK_IN_MILLIS) {
      count = Math.abs(dayDistance(tz, time, now));
      unit = UDAT_RELATIVE_DAYS;

      if (count == 2) {
        // Some locale has a special term for "2 days ago". Return it if available.
        address = getFormatter(locale.toString(), style);
        String str;
        if (past) {
          synchronized (CACHED_FORMATTERS) {
            str = formatWithAbsoluteUnit(address, UDAT_DIRECTION_LAST_2, UDAT_ABSOLUTE_DAY);
          }
        } else {
          synchronized (CACHED_FORMATTERS) {
            str = formatWithAbsoluteUnit(address, UDAT_DIRECTION_NEXT_2, UDAT_ABSOLUTE_DAY);
          }
        }
        if (str != null && !str.isEmpty()) {
          return str;
        }
      } else if (count == 1) {
        // Show "yesterday / tomorrrow" instead of "1 day ago / in 1 day".
        unit = UDAT_ABSOLUTE_DAY;
        relative = false;
      } else if (count == 0) {
        // Show "today" if time and now are on the same day.
        unit = UDAT_ABSOLUTE_DAY;
        direction = UDAT_DIRECTION_THIS;
        relative = false;
      }
    } else if (minResolution == WEEK_IN_MILLIS) {
      count = (int)(duration / WEEK_IN_MILLIS);
      unit = UDAT_RELATIVE_WEEKS;
    } else {
      // The duration is longer than a week and minResolution is not WEEK_IN_MILLIS. Return the
      // absolute date instead of relative time.
      return DateIntervalFormat.formatDateRange(locale, tz, time, time, flags);
    }

    if (address == 0) {
      address = getFormatter(locale.toString(), style);
    }

    if (relative) {
      synchronized (CACHED_FORMATTERS) {
        return formatWithRelativeUnit(address, count, direction, unit);
      }
    } else {
      synchronized (CACHED_FORMATTERS) {
        return formatWithAbsoluteUnit(address, direction, unit);
      }
    }
  }

  // This is public DateUtils API in frameworks/base.
  public static String getRelativeDateTimeString(long time, long minResolution,
      long transitionResolution, int flags) {
    long now = System.currentTimeMillis();
    return getRelativeDateTimeString(Locale.getDefault(), TimeZone.getDefault(), time, now,
                                     minResolution, transitionResolution, flags);
  }

  // Internal API that accepts locale and timezone
  public static String getRelativeDateTimeString(Locale locale, TimeZone tz, long time, long now,
                                                 long minResolution, long transitionResolution,
                                                 int flags) {

    if (locale == null) {
      locale = Locale.getDefault();
    }
    if (tz == null) {
      tz = TimeZone.getDefault();
    }

    // Get the time clause first.
    String timeClause = DateIntervalFormat.formatDateRange(locale, tz, time, time, FORMAT_SHOW_TIME);

    long duration = Math.abs(now - time);
    // It doesn't make much sense to have results like: "1 week ago, 10:50 AM".
    if (transitionResolution > WEEK_IN_MILLIS) {
        transitionResolution = WEEK_IN_MILLIS;
    }
    // Use UDAT_STYLE_SHORT or UDAT_STYLE_LONG.
    int style;
    if ((flags & (FORMAT_ABBREV_RELATIVE | FORMAT_ABBREV_ALL)) != 0) {
        style = UDAT_STYLE_SHORT;
    } else {
        style = UDAT_STYLE_LONG;
    }

    Calendar timeCalendar = Calendar.getInstance(tz);
    timeCalendar.setTimeInMillis(time);
    Calendar nowCalendar = Calendar.getInstance(tz);
    nowCalendar.setTimeInMillis(now);

    int days = Math.abs(dayDistance(timeCalendar, nowCalendar));
    long address = getFormatter(locale.toString(), style);

    // Now get the date clause, either in relative format or the actual date.
    String dateClause;
    if (duration < transitionResolution) {
      // This is to fix bug 5252772. If there is any date difference, we should promote the
      // minResolution to DAY_IN_MILLIS so that it can display the date instead of
      // "x hours/minutes ago, [time]".
      if (days > 0 && minResolution < DAY_IN_MILLIS) {
         minResolution = DAY_IN_MILLIS;
      }
      dateClause = getRelativeTimeSpanString(locale, tz, time, now, minResolution, flags);
    } else {
      // We are dropping user-supplied flags to format the date clause.
      if (days == 0) {
        // Same day
        flags = FORMAT_SHOW_TIME;
      } else if (timeCalendar.get(Calendar.YEAR) != nowCalendar.get(Calendar.YEAR)) {
        // Different years
        flags = FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR | FORMAT_NUMERIC_DATE;
      } else {
        // Default
        flags = FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH;
      }

      dateClause = DateIntervalFormat.formatDateRange(locale, tz, time, time, flags);
    }

    // Combine the two clauses, such as '5 days ago, 10:50 AM'.
    return combineDateAndTime(address, dateClause, timeClause);
  }

  private static long getFormatter(String localeName, int style) {
    String key = localeName + "\t" + style;
    Long formatter = CACHED_FORMATTERS.get(key);
    if (formatter == null) {
      formatter = createRelativeDateTimeFormatter(localeName, style);
      CACHED_FORMATTERS.put(key, formatter);
    }
    return formatter;
  }

  private static int dayDistance(TimeZone tz, long startTime, long endTime) {
    if (tz == null) {
      tz = TimeZone.getDefault();
    }
    Calendar startCalendar = Calendar.getInstance(tz);
    Calendar endCalendar = Calendar.getInstance(tz);
    startCalendar.setTimeInMillis(startTime);
    endCalendar.setTimeInMillis(endTime);
    return dayDistance(startCalendar, endCalendar);
  }

  private static int dayDistance(Calendar c1, Calendar c2) {
    return julianDay(c2) - julianDay(c1);
  }

  private static int julianDay(Calendar c) {
    long utcMs = c.getTimeInMillis() + c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET);
    return (int) (utcMs / DAY_IN_MILLIS) + EPOCH_JULIAN_DAY;
  }

  private static native long createRelativeDateTimeFormatter(String localeName, int style);
  private static native void destroyRelativeDateTimeFormatter(long address);
  private static native String formatWithRelativeUnit(long address, int quantity, int direction, int unit);
  private static native String formatWithAbsoluteUnit(long address, int direction, int unit);
  private static native String combineDateAndTime(long address, String relativeDateString, String timeString);
}
