package libcore.icu;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import static libcore.icu.RelativeDateTimeFormatter.*;

public class RelativeDateTimeFormatterTest extends junit.framework.TestCase {

  // Tests adopted from CTS tests for DateUtils.getRelativeTimeSpanString.
  public void test_getRelativeTimeSpanStringCTS() throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    final long baseTime = System.currentTimeMillis();

    assertEquals("0 minutes ago",
                 getRelativeTimeSpanString(baseTime - SECOND_IN_MILLIS, baseTime,
                                           MINUTE_IN_MILLIS, 0));
    assertEquals("in 0 minutes",
                 getRelativeTimeSpanString(baseTime + SECOND_IN_MILLIS, baseTime,
                                           MINUTE_IN_MILLIS, 0));

    assertEquals("1 minute ago",
                 getRelativeTimeSpanString(0, MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, 0));
    assertEquals("in 1 minute",
                 getRelativeTimeSpanString(MINUTE_IN_MILLIS, 0, MINUTE_IN_MILLIS, 0));

    assertEquals("42 minutes ago",
      getRelativeTimeSpanString(baseTime - (42 * MINUTE_IN_MILLIS), baseTime, MINUTE_IN_MILLIS, 0));
    assertEquals("in 42 minutes",
      getRelativeTimeSpanString(baseTime + (42 * MINUTE_IN_MILLIS), baseTime, MINUTE_IN_MILLIS, 0));

    final long TWO_HOURS_IN_MS = 2 * HOUR_IN_MILLIS;
    assertEquals("2 hours ago",
                 getRelativeTimeSpanString(baseTime - TWO_HOURS_IN_MS, baseTime, MINUTE_IN_MILLIS,
                                           FORMAT_NUMERIC_DATE));
    assertEquals("in 2 hours",
                 getRelativeTimeSpanString(baseTime + TWO_HOURS_IN_MS, baseTime, MINUTE_IN_MILLIS,
                                           FORMAT_NUMERIC_DATE));

    assertEquals("in 42 min.",
                 getRelativeTimeSpanString(baseTime + (42 * MINUTE_IN_MILLIS), baseTime,
                                           MINUTE_IN_MILLIS, FORMAT_ABBREV_RELATIVE));

    assertEquals("tomorrow",
                 getRelativeTimeSpanString(DAY_IN_MILLIS, 0, DAY_IN_MILLIS, 0));
    assertEquals("in 2 days",
                 getRelativeTimeSpanString(2 * DAY_IN_MILLIS, 0, DAY_IN_MILLIS, 0));
    assertEquals("yesterday",
                 getRelativeTimeSpanString(0, DAY_IN_MILLIS, DAY_IN_MILLIS, 0));
    assertEquals("2 days ago",
                 getRelativeTimeSpanString(0, 2 * DAY_IN_MILLIS, DAY_IN_MILLIS, 0));

    final long DAY_DURATION = 5 * 24 * 60 * 60 * 1000;
    assertEquals("5 days ago",
                 getRelativeTimeSpanString(baseTime - DAY_DURATION, baseTime, DAY_IN_MILLIS, 0));
  }

  public void test_getRelativeTimeSpanString() throws Exception {

    final Object[][] relativeTimes = {
      // {timeDiff, minResolution, expectedInPast, expectedInFuture}
      {0 * SECOND_IN_MILLIS, 0, "0 seconds ago", "0 seconds ago"},
      {1 * MINUTE_IN_MILLIS, 0, "1 minute ago", "in 1 minute"},
      {5 * DAY_IN_MILLIS, 0, "5 days ago", "in 5 days"},

      {0 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "0 seconds ago", "0 seconds ago"},
      {1 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "1 second ago", "in 1 second"},
      {2 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "2 seconds ago", "in 2 seconds"},
      {25 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "25 seconds ago", "in 25 seconds"},
      {75 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "1 minute ago", "in 1 minute"},
      {5000 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "1 hour ago", "in 1 hour"},

      {0 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "0 minutes ago", "0 minutes ago"},
      {1 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "1 minute ago", "in 1 minute"},
      {2 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "2 minutes ago", "in 2 minutes"},
      {25 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "25 minutes ago", "in 25 minutes"},
      {75 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "1 hour ago", "in 1 hour"},
      {720 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "12 hours ago", "in 12 hours"},

      {0 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "0 hours ago", "0 hours ago"},
      {1 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "1 hour ago", "in 1 hour"},
      {2 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "2 hours ago", "in 2 hours"},
      {5 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "5 hours ago", "in 5 hours"},
      {20 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "20 hours ago", "in 20 hours"},

      {0 * DAY_IN_MILLIS, DAY_IN_MILLIS, "today", "today"},
      {20 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "yesterday", "tomorrow"},
      {24 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "yesterday", "tomorrow"},
      {2 * DAY_IN_MILLIS, DAY_IN_MILLIS, "2 days ago", "in 2 days"},
      {25 * DAY_IN_MILLIS, DAY_IN_MILLIS, "January 11", "March 2"},

      {0 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "0 weeks ago", "0 weeks ago"},
      {1 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "1 week ago", "in 1 week"},
      {2 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "2 weeks ago", "in 2 weeks"},
      {25 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "25 weeks ago", "in 25 weeks"},

      // duration >= minResolution
      {30 * SECOND_IN_MILLIS, 0, "30 seconds ago", "in 30 seconds"},
      {30 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "30 minutes ago", "in 30 minutes"},
      {30 * HOUR_IN_MILLIS, MINUTE_IN_MILLIS, "yesterday", "tomorrow"},
      {5 * DAY_IN_MILLIS, MINUTE_IN_MILLIS, "5 days ago", "in 5 days"},
      {30 * WEEK_IN_MILLIS, MINUTE_IN_MILLIS, "July 10, 2014", "September 3"},
      {5 * YEAR_IN_MILLIS, MINUTE_IN_MILLIS, "February 11, 2010", "January 30, 2020"},

      {60 * SECOND_IN_MILLIS, MINUTE_IN_MILLIS, "1 minute ago", "in 1 minute"},
      {120 * SECOND_IN_MILLIS - 1, MINUTE_IN_MILLIS, "1 minute ago", "in 1 minute"},
      {60 * MINUTE_IN_MILLIS, HOUR_IN_MILLIS, "1 hour ago", "in 1 hour"},
      {120 * MINUTE_IN_MILLIS - 1, HOUR_IN_MILLIS, "1 hour ago", "in 1 hour"},
      {2 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "today", "today"},
      {12 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "yesterday", "today"},
      {24 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "yesterday", "tomorrow"},
      {48 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "2 days ago", "in 2 days"},
      {45 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "2 days ago", "in 2 days"},
      {7 * DAY_IN_MILLIS, WEEK_IN_MILLIS, "1 week ago", "in 1 week"},
      {14 * DAY_IN_MILLIS - 1, WEEK_IN_MILLIS, "1 week ago", "in 1 week"},

      // duration < minResolution
      {59 * SECOND_IN_MILLIS, MINUTE_IN_MILLIS, "0 minutes ago", "in 0 minutes"},
      {59 * MINUTE_IN_MILLIS, HOUR_IN_MILLIS, "0 hours ago", "in 0 hours"},
      {HOUR_IN_MILLIS - 1, HOUR_IN_MILLIS, "0 hours ago", "in 0 hours"},
      {DAY_IN_MILLIS - 1, DAY_IN_MILLIS, "yesterday", "tomorrow"},
      {20 * SECOND_IN_MILLIS, WEEK_IN_MILLIS, "0 weeks ago", "in 0 weeks"},
      {WEEK_IN_MILLIS - 1, WEEK_IN_MILLIS, "0 weeks ago", "in 0 weeks"},
    };

    Locale locale = Locale.US;
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    Calendar cal = Calendar.getInstance(tz, locale);
    // Feb 5, 2015 at 10:50 PST
    cal.set(2015, Calendar.FEBRUARY, 5, 10, 50, 0);
    long base = cal.getTimeInMillis();

    for (Object[] row : relativeTimes) {
      long delta = ((Number)row[0]).longValue();
      long minResolution = ((Number)row[1]).longValue();
      int flags = 0;
      // in the past
      assertEquals(row[2], getRelativeTimeSpanString(locale, tz, base - delta, base,
                                                     minResolution, flags));
      // in the future
      assertEquals(row[3], getRelativeTimeSpanString(locale, tz, base + delta, base,
                                                     minResolution, flags));
    }
  }

  public void test_getRelativeTimeSpanStringAbbrev() throws Exception {

    final Object[][] relativeTimesAbbrev = {
      // {timeDiff, minResolution, expectedInThePast, expectedInTheFuture}
      {0 * SECOND_IN_MILLIS, 0, "0 sec. ago", "0 sec. ago"},
      {1 * MINUTE_IN_MILLIS, 0, "1 min. ago", "in 1 min."},
      {5 * DAY_IN_MILLIS, 0, "5 days ago", "in 5 days"},

      {0 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "0 sec. ago", "0 sec. ago"},
      {1 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "1 sec. ago", "in 1 sec."},
      {2 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "2 sec. ago", "in 2 sec."},
      {25 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "25 sec. ago", "in 25 sec."},
      {75 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "1 min. ago", "in 1 min."},
      {5000 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "1 hr. ago", "in 1 hr."},

      {0 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "0 min. ago", "0 min. ago"},
      {1 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "1 min. ago", "in 1 min."},
      {2 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "2 min. ago", "in 2 min."},
      {25 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "25 min. ago", "in 25 min."},
      {75 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "1 hr. ago", "in 1 hr."},
      {720 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "12 hr. ago", "in 12 hr."},

      {0 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "0 hr. ago", "0 hr. ago"},
      {1 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "1 hr. ago", "in 1 hr."},
      {2 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "2 hr. ago", "in 2 hr."},
      {5 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "5 hr. ago", "in 5 hr."},
      {20 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "20 hr. ago", "in 20 hr."},

      {0 * DAY_IN_MILLIS, DAY_IN_MILLIS, "today", "today"},
      {20 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "yesterday", "tomorrow"},
      {24 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "yesterday", "tomorrow"},
      {2 * DAY_IN_MILLIS, DAY_IN_MILLIS, "2 days ago", "in 2 days"},
      {25 * DAY_IN_MILLIS, DAY_IN_MILLIS, "January 11", "March 2"},

      {0 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "0 wk. ago", "0 wk. ago"},
      {1 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "1 wk. ago", "in 1 wk."},
      {2 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "2 wk. ago", "in 2 wk."},
      {25 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "25 wk. ago", "in 25 wk."},

      // duration >= minResolution
      {30 * SECOND_IN_MILLIS, 0, "30 sec. ago", "in 30 sec."},
      {30 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "30 min. ago", "in 30 min."},
      {30 * HOUR_IN_MILLIS, MINUTE_IN_MILLIS, "yesterday", "tomorrow"},
      {5 * DAY_IN_MILLIS, MINUTE_IN_MILLIS, "5 days ago", "in 5 days"},
      {30 * WEEK_IN_MILLIS, MINUTE_IN_MILLIS, "July 10, 2014", "September 3"},
      {5 * YEAR_IN_MILLIS, MINUTE_IN_MILLIS, "February 11, 2010", "January 30, 2020"},

      {60 * SECOND_IN_MILLIS, MINUTE_IN_MILLIS, "1 min. ago", "in 1 min."},
      {120 * SECOND_IN_MILLIS - 1, MINUTE_IN_MILLIS, "1 min. ago", "in 1 min."},
      {60 * MINUTE_IN_MILLIS, HOUR_IN_MILLIS, "1 hr. ago", "in 1 hr."},
      {120 * MINUTE_IN_MILLIS - 1, HOUR_IN_MILLIS, "1 hr. ago", "in 1 hr."},
      {2 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "today", "today"},
      {12 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "yesterday", "today"},
      {24 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "yesterday", "tomorrow"},
      {48 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "2 days ago", "in 2 days"},
      {45 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "2 days ago", "in 2 days"},
      {7 * DAY_IN_MILLIS, WEEK_IN_MILLIS, "1 wk. ago", "in 1 wk."},
      {14 * DAY_IN_MILLIS - 1, WEEK_IN_MILLIS, "1 wk. ago", "in 1 wk."},

      // duration < minResolution
      {59 * SECOND_IN_MILLIS, MINUTE_IN_MILLIS, "0 min. ago", "in 0 min."},
      {59 * MINUTE_IN_MILLIS, HOUR_IN_MILLIS, "0 hr. ago", "in 0 hr."},
      {HOUR_IN_MILLIS - 1, HOUR_IN_MILLIS, "0 hr. ago", "in 0 hr."},
      {DAY_IN_MILLIS - 1, DAY_IN_MILLIS, "yesterday", "tomorrow"},
      {20 * SECOND_IN_MILLIS, WEEK_IN_MILLIS, "0 wk. ago", "in 0 wk."},
      {WEEK_IN_MILLIS - 1, WEEK_IN_MILLIS, "0 wk. ago", "in 0 wk."},
    };

    Locale locale = Locale.US;
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    Calendar cal = Calendar.getInstance(tz, locale);
    // Feb 5, 2015 at 10:50 PST
    cal.set(2015, Calendar.FEBRUARY, 5, 10, 50, 0);
    long base = cal.getTimeInMillis();

    for (Object[] row : relativeTimesAbbrev) {
      long delta = ((Number)row[0]).longValue();
      long minResolution = ((Number)row[1]).longValue();
      int flags = FORMAT_ABBREV_RELATIVE;
      // in the past
      assertEquals(row[2], getRelativeTimeSpanString(locale, tz, base - delta, base,
                                                     minResolution, flags));
      // in the future
      assertEquals(row[3], getRelativeTimeSpanString(locale, tz, base + delta, base,
                                                     minResolution, flags));
    }
  }

  public void test_getRelativeTimeSpanStringGerman() throws Exception {
    Locale locale = Locale.GERMAN;
    final long now = System.currentTimeMillis();

    // 42 minutes ago
    assertEquals("vor 42 Minuten",
      getRelativeTimeSpanString(locale, null, now - 42 * MINUTE_IN_MILLIS, now,
                                MINUTE_IN_MILLIS, 0));
    // in 42 minutes
    assertEquals("in 42 Minuten",
      getRelativeTimeSpanString(locale, null, now + 42 * MINUTE_IN_MILLIS, now,
                                MINUTE_IN_MILLIS, 0));
    // yesterday
    assertEquals("gestern",
                 getRelativeTimeSpanString(locale, null, now - DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // the day before yesterday
    assertEquals("vorgestern",
                 getRelativeTimeSpanString(locale, null, now - 2 * DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // tomorrow
    assertEquals("morgen",
                 getRelativeTimeSpanString(locale, null, now + DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // the day after tomorrow
    assertEquals("\u00FCbermorgen",
                 getRelativeTimeSpanString(locale, null, now + 2 * DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
  }

  public void test_getRelativeTimeSpanStringFrench() throws Exception {
    Locale locale = Locale.FRENCH;
    final long now = System.currentTimeMillis();

    // 42 minutes ago
    assertEquals("il y a 42 minutes",
                 getRelativeTimeSpanString(locale, null, now - (42 * MINUTE_IN_MILLIS), now,
                                           MINUTE_IN_MILLIS, 0));
    // in 42 minutes
    assertEquals("dans 42 minutes",
                 getRelativeTimeSpanString(locale, null, now + (42 * MINUTE_IN_MILLIS), now,
                                           MINUTE_IN_MILLIS, 0));
    // yesterday
    assertEquals("hier",
                 getRelativeTimeSpanString(locale, null, now - DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // the day before yesterday
    assertEquals("avant-hier",
                 getRelativeTimeSpanString(locale, null, now - 2 * DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // tomorrow
    assertEquals("demain",
                 getRelativeTimeSpanString(locale, null, now + DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // the day after tomorrow
    assertEquals("apr\u00E8s-demain",
                 getRelativeTimeSpanString(locale, null, now + 2 * DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
  }

  // Tests adopted from CTS tests for DateUtils.getRelativeDateTimeString.
  public void test_getRelativeDateTimeStringCTS() throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    final long baseTime = System.currentTimeMillis();

    final long DAY_DURATION = 5 * 24 * 60 * 60 * 1000;
    assertNotNull(getRelativeDateTimeString(baseTime - DAY_DURATION, MINUTE_IN_MILLIS,
                                            DAY_IN_MILLIS, FORMAT_NUMERIC_DATE));
  }

  public void test_getRelativeDateTimeString() throws Exception {
    Locale locale = Locale.US;
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    Calendar cal = Calendar.getInstance(tz, locale);
    // Feb 5, 2015 at 10:50 PST
    cal.set(2015, Calendar.FEBRUARY, 5, 10, 50, 0);
    long base = cal.getTimeInMillis();

    assertEquals("5 seconds ago, 10:49 AM",
                 getRelativeDateTimeString(locale, tz, base - 5 * SECOND_IN_MILLIS, base, 0, MINUTE_IN_MILLIS, 0));
    assertEquals("5 min. ago, 10:45 AM",
                 getRelativeDateTimeString(locale, tz, base - 5 * MINUTE_IN_MILLIS, base, 0, HOUR_IN_MILLIS,
                                           FORMAT_ABBREV_RELATIVE));
    assertEquals("0 hr. ago, 10:45 AM",
                 getRelativeDateTimeString(locale, tz, base - 5 * MINUTE_IN_MILLIS, base, HOUR_IN_MILLIS,
                                           DAY_IN_MILLIS, FORMAT_ABBREV_RELATIVE));
    assertEquals("5 hours ago, 5:50 AM",
                 getRelativeDateTimeString(locale, tz, base - 5 * HOUR_IN_MILLIS, base, HOUR_IN_MILLIS,
                                           DAY_IN_MILLIS, 0));
    assertEquals("yesterday, 7:50 PM",
                 getRelativeDateTimeString(locale, tz, base - 15 * HOUR_IN_MILLIS, base, 0, WEEK_IN_MILLIS,
                                           FORMAT_ABBREV_RELATIVE));
    assertEquals("5 days ago, 10:50 AM",
                 getRelativeDateTimeString(locale, tz, base - 5 * DAY_IN_MILLIS, base, 0, WEEK_IN_MILLIS, 0));
    assertEquals("Jan 29, 10:50 AM",
                 getRelativeDateTimeString(locale, tz, base - 7 * DAY_IN_MILLIS, base, 0, WEEK_IN_MILLIS, 0));
    assertEquals("11/27/2014, 10:50 AM",
                 getRelativeDateTimeString(locale, tz, base - 10 * WEEK_IN_MILLIS, base, 0, WEEK_IN_MILLIS, 0));
    assertEquals("11/27/2014, 10:50 AM",
                 getRelativeDateTimeString(locale, tz, base - 10 * WEEK_IN_MILLIS, base, 0, YEAR_IN_MILLIS, 0));

    // User-supplied flags should be ignored when formatting the date clause.
    final int FORMAT_SHOW_WEEKDAY = 0x00002;
    assertEquals("11/27/2014, 10:50 AM",
                 getRelativeDateTimeString(locale, tz, base - 10 * WEEK_IN_MILLIS, base, 0, WEEK_IN_MILLIS,
                                           FORMAT_ABBREV_ALL | FORMAT_SHOW_WEEKDAY));
  }

  public void test_getRelativeDateTimeStringItalian() throws Exception {
    Locale it_IT = new Locale("it", "IT");
    TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
    Calendar cal = Calendar.getInstance(tz, it_IT);
    // 05 febbraio 2015 20:15
    cal.set(2015, Calendar.FEBRUARY, 5, 20, 15, 0);
    long base = cal.getTimeInMillis();

    assertEquals("5 secondi fa, 20:14",
                 getRelativeDateTimeString(it_IT, tz, base - 5 * SECOND_IN_MILLIS, base, 0,
                                           MINUTE_IN_MILLIS, 0));
    assertEquals("5 min. fa, 20:10",
                 getRelativeDateTimeString(it_IT, tz, base - 5 * MINUTE_IN_MILLIS, base, 0,
                                           HOUR_IN_MILLIS, FORMAT_ABBREV_RELATIVE));
    assertEquals("0 h. fa, 20:10",
                 getRelativeDateTimeString(it_IT, tz, base - 5 * MINUTE_IN_MILLIS, base,
                                           HOUR_IN_MILLIS, DAY_IN_MILLIS, FORMAT_ABBREV_RELATIVE));
    assertEquals("ieri, 22:15",
                 getRelativeDateTimeString(it_IT, tz, base - 22 * HOUR_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, FORMAT_ABBREV_RELATIVE));
    assertEquals("5 giorni fa, 20:15",
                 getRelativeDateTimeString(it_IT, tz, base - 5 * DAY_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, 0));
    assertEquals("27/11/2014, 20:15",
                 getRelativeDateTimeString(it_IT, tz, base - 10 * WEEK_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, 0));
  }

  // http://b/5252772: detect the actual date difference
  public void test5252772() throws Exception {
    Locale locale = Locale.US;
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");

    // Now is Sep 2, 2011, 10:23 AM PDT.
    Calendar nowCalendar = Calendar.getInstance(tz, locale);
    nowCalendar.set(2011, Calendar.SEPTEMBER, 2, 10, 23, 0);
    final long now = nowCalendar.getTimeInMillis();

    // Sep 1, 2011, 10:24 AM
    Calendar yesterdayCalendar1 = Calendar.getInstance(tz, locale);
    yesterdayCalendar1.set(2011, Calendar.SEPTEMBER, 1, 10, 24, 0);
    long yesterday1 = yesterdayCalendar1.getTimeInMillis();
    assertEquals("yesterday, 10:24 AM",
                 getRelativeDateTimeString(locale, tz, yesterday1, now, MINUTE_IN_MILLIS,
                                          WEEK_IN_MILLIS, 0));

    // Sep 1, 2011, 10:22 AM
    Calendar yesterdayCalendar2 = Calendar.getInstance(tz, locale);
    yesterdayCalendar2.set(2011, Calendar.SEPTEMBER, 1, 10, 22, 0);
    long yesterday2 = yesterdayCalendar2.getTimeInMillis();
    assertEquals("yesterday, 10:22 AM",
                 getRelativeDateTimeString(locale, tz, yesterday2, now, MINUTE_IN_MILLIS,
                                          WEEK_IN_MILLIS, 0));

    // Aug 31, 2011, 10:24 AM
    Calendar twoDaysAgoCalendar1 = Calendar.getInstance(tz, locale);
    twoDaysAgoCalendar1.set(2011, Calendar.AUGUST, 31, 10, 24, 0);
    long twoDaysAgo1 = twoDaysAgoCalendar1.getTimeInMillis();
    assertEquals("2 days ago, 10:24 AM",
                 getRelativeDateTimeString(locale, tz, twoDaysAgo1, now, MINUTE_IN_MILLIS,
                                          WEEK_IN_MILLIS, 0));

    // Aug 31, 2011, 10:22 AM
    Calendar twoDaysAgoCalendar2 = Calendar.getInstance(tz, locale);
    twoDaysAgoCalendar2.set(2011, Calendar.AUGUST, 31, 10, 22, 0);
    long twoDaysAgo2 = twoDaysAgoCalendar2.getTimeInMillis();
    assertEquals("2 days ago, 10:22 AM",
                 getRelativeDateTimeString(locale, tz, twoDaysAgo2, now, MINUTE_IN_MILLIS,
                                          WEEK_IN_MILLIS, 0));

    // Sep 3, 2011, 10:22 AM
    Calendar tomorrowCalendar1 = Calendar.getInstance(tz, locale);
    tomorrowCalendar1.set(2011, Calendar.SEPTEMBER, 3, 10, 22, 0);
    long tomorrow1 = tomorrowCalendar1.getTimeInMillis();
    assertEquals("tomorrow, 10:22 AM",
                 getRelativeDateTimeString(locale, tz, tomorrow1, now, MINUTE_IN_MILLIS,
                                          WEEK_IN_MILLIS, 0));

    // Sep 3, 2011, 10:24 AM
    Calendar tomorrowCalendar2 = Calendar.getInstance(tz, locale);
    tomorrowCalendar2.set(2011, Calendar.SEPTEMBER, 3, 10, 24, 0);
    long tomorrow2 = tomorrowCalendar2.getTimeInMillis();
    assertEquals("tomorrow, 10:24 AM",
                 getRelativeDateTimeString(locale, tz, tomorrow2, now, MINUTE_IN_MILLIS,
                                          WEEK_IN_MILLIS, 0));

    // Sep 4, 2011, 10:22 AM
    Calendar twoDaysLaterCalendar1 = Calendar.getInstance(tz, locale);
    twoDaysLaterCalendar1.set(2011, Calendar.SEPTEMBER, 4, 10, 22, 0);
    long twoDaysLater1 = twoDaysLaterCalendar1.getTimeInMillis();
    assertEquals("in 2 days, 10:22 AM",
                 getRelativeDateTimeString(locale, tz, twoDaysLater1, now, MINUTE_IN_MILLIS,
                                          WEEK_IN_MILLIS, 0));

    // Sep 4, 2011, 10:24 AM
    Calendar twoDaysLaterCalendar2 = Calendar.getInstance(tz, locale);
    twoDaysLaterCalendar2.set(2011, Calendar.SEPTEMBER, 4, 10, 24, 0);
    long twoDaysLater2 = twoDaysLaterCalendar2.getTimeInMillis();
    assertEquals("in 2 days, 10:24 AM",
                 getRelativeDateTimeString(locale, tz, twoDaysLater2, now, MINUTE_IN_MILLIS,
                                          WEEK_IN_MILLIS, 0));
  }
}
