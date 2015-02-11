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
import static libcore.icu.RelativeDateTimeFormatter.getRelativeDateTimeString;
import static libcore.icu.RelativeDateTimeFormatter.getRelativeTimeSpanString;
import static libcore.icu.RelativeDateTimeFormatter.FORMAT_ABBREV_ALL;
import static libcore.icu.RelativeDateTimeFormatter.FORMAT_ABBREV_RELATIVE;
import static libcore.icu.RelativeDateTimeFormatter.FORMAT_NUMERIC_DATE;
import static libcore.icu.RelativeDateTimeFormatter.SECOND_IN_MILLIS;
import static libcore.icu.RelativeDateTimeFormatter.MINUTE_IN_MILLIS;
import static libcore.icu.RelativeDateTimeFormatter.HOUR_IN_MILLIS;
import static libcore.icu.RelativeDateTimeFormatter.DAY_IN_MILLIS;
import static libcore.icu.RelativeDateTimeFormatter.WEEK_IN_MILLIS;
import static libcore.icu.RelativeDateTimeFormatter.YEAR_IN_MILLIS;

public class RelativeDateTimeFormatterTest extends junit.framework.TestCase {

  // Tests adopted from CTS tests for DateUtils.getRelativeTimeSpanString.
  public void test_getRelativeTimeSpanStringCTS() throws Exception {
    Locale en_US = new Locale("en", "US");
    TimeZone tz = TimeZone.getTimeZone("GMT");
    Calendar cal = Calendar.getInstance(tz, en_US);
    // Feb 5, 2015 at 10:50 GMT
    cal.set(2015, Calendar.FEBRUARY, 5, 10, 50, 0);
    final long baseTime = cal.getTimeInMillis();

    assertEquals("0 minutes ago",
                 getRelativeTimeSpanString(en_US, tz, baseTime - SECOND_IN_MILLIS, baseTime,
                                           MINUTE_IN_MILLIS, 0));
    assertEquals("in 0 minutes",
                 getRelativeTimeSpanString(en_US, tz, baseTime + SECOND_IN_MILLIS, baseTime,
                                           MINUTE_IN_MILLIS, 0));

    assertEquals("1 minute ago",
                 getRelativeTimeSpanString(en_US, tz, 0, MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, 0));
    assertEquals("in 1 minute",
                 getRelativeTimeSpanString(en_US, tz, MINUTE_IN_MILLIS, 0, MINUTE_IN_MILLIS, 0));

    assertEquals("42 minutes ago",
      getRelativeTimeSpanString(en_US, tz, baseTime - 42 * MINUTE_IN_MILLIS, baseTime,
                                MINUTE_IN_MILLIS, 0));
    assertEquals("in 42 minutes",
      getRelativeTimeSpanString(en_US, tz, baseTime + 42 * MINUTE_IN_MILLIS, baseTime,
                                MINUTE_IN_MILLIS, 0));

    final long TWO_HOURS_IN_MS = 2 * HOUR_IN_MILLIS;
    assertEquals("2 hours ago",
                 getRelativeTimeSpanString(en_US, tz, baseTime - TWO_HOURS_IN_MS, baseTime,
                                           MINUTE_IN_MILLIS, FORMAT_NUMERIC_DATE));
    assertEquals("in 2 hours",
                 getRelativeTimeSpanString(en_US, tz, baseTime + TWO_HOURS_IN_MS, baseTime,
                                           MINUTE_IN_MILLIS, FORMAT_NUMERIC_DATE));

    assertEquals("in 42 min.",
                 getRelativeTimeSpanString(en_US, tz, baseTime + (42 * MINUTE_IN_MILLIS), baseTime,
                                           MINUTE_IN_MILLIS, FORMAT_ABBREV_RELATIVE));

    assertEquals("Tomorrow",
                 getRelativeTimeSpanString(en_US, tz, DAY_IN_MILLIS, 0, DAY_IN_MILLIS, 0));
    assertEquals("in 2 days",
                 getRelativeTimeSpanString(en_US, tz, 2 * DAY_IN_MILLIS, 0, DAY_IN_MILLIS, 0));
    assertEquals("Yesterday",
                 getRelativeTimeSpanString(en_US, tz, 0, DAY_IN_MILLIS, DAY_IN_MILLIS, 0));
    assertEquals("2 days ago",
                 getRelativeTimeSpanString(en_US, tz, 0, 2 * DAY_IN_MILLIS, DAY_IN_MILLIS, 0));

    final long DAY_DURATION = 5 * 24 * 60 * 60 * 1000;
    assertEquals("5 days ago",
                 getRelativeTimeSpanString(en_US, tz, baseTime - DAY_DURATION, baseTime,
                                           DAY_IN_MILLIS, 0));
  }

  private void test_getRelativeTimeSpanString_helper(long delta, long minResolution, int flags,
                                                     String expectedInPast,
                                                     String expectedInFuture) throws Exception {
    Locale en_US = new Locale("en", "US");
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    Calendar cal = Calendar.getInstance(tz, en_US);
    // Feb 5, 2015 at 10:50 PST
    cal.set(2015, Calendar.FEBRUARY, 5, 10, 50, 0);
    final long base = cal.getTimeInMillis();

    assertEquals(expectedInPast,
                 getRelativeTimeSpanString(en_US, tz, base - delta, base, minResolution, flags));
    assertEquals(expectedInFuture,
                 getRelativeTimeSpanString(en_US, tz, base + delta, base, minResolution, flags));
  }

  private void test_getRelativeTimeSpanString_helper(long delta, long minResolution,
                                                     String expectedInPast,
                                                     String expectedInFuture) throws Exception {
    test_getRelativeTimeSpanString_helper(delta, minResolution, 0, expectedInPast, expectedInFuture);
  }

  public void test_getRelativeTimeSpanString() throws Exception {

    test_getRelativeTimeSpanString_helper(0 * SECOND_IN_MILLIS, 0, "0 seconds ago", "0 seconds ago");
    test_getRelativeTimeSpanString_helper(1 * MINUTE_IN_MILLIS, 0, "1 minute ago", "in 1 minute");
    test_getRelativeTimeSpanString_helper(1 * MINUTE_IN_MILLIS, 0, "1 minute ago", "in 1 minute");
    test_getRelativeTimeSpanString_helper(5 * DAY_IN_MILLIS, 0, "5 days ago", "in 5 days");

    test_getRelativeTimeSpanString_helper(0 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "0 seconds ago",
                                          "0 seconds ago");
    test_getRelativeTimeSpanString_helper(1 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "1 second ago",
                                          "in 1 second");
    test_getRelativeTimeSpanString_helper(2 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "2 seconds ago",
                                          "in 2 seconds");
    test_getRelativeTimeSpanString_helper(25 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "25 seconds ago",
                                          "in 25 seconds");
    test_getRelativeTimeSpanString_helper(75 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "1 minute ago",
                                          "in 1 minute");
    test_getRelativeTimeSpanString_helper(5000 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, "1 hour ago",
                                          "in 1 hour");

    test_getRelativeTimeSpanString_helper(0 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "0 minutes ago",
                                          "0 minutes ago");
    test_getRelativeTimeSpanString_helper(1 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "1 minute ago",
                                          "in 1 minute");
    test_getRelativeTimeSpanString_helper(2 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "2 minutes ago",
                                          "in 2 minutes");
    test_getRelativeTimeSpanString_helper(25 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "25 minutes ago",
                                          "in 25 minutes");
    test_getRelativeTimeSpanString_helper(75 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "1 hour ago",
                                          "in 1 hour");
    test_getRelativeTimeSpanString_helper(720 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, "12 hours ago",
                                          "in 12 hours");

    test_getRelativeTimeSpanString_helper(0 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "0 hours ago",
                                          "0 hours ago");
    test_getRelativeTimeSpanString_helper(1 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "1 hour ago",
                                          "in 1 hour");
    test_getRelativeTimeSpanString_helper(2 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "2 hours ago",
                                          "in 2 hours");
    test_getRelativeTimeSpanString_helper(5 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "5 hours ago",
                                          "in 5 hours");
    test_getRelativeTimeSpanString_helper(20 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, "20 hours ago",
                                          "in 20 hours");

    test_getRelativeTimeSpanString_helper(0 * DAY_IN_MILLIS, DAY_IN_MILLIS, "Today", "Today");
    test_getRelativeTimeSpanString_helper(20 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "Yesterday",
                                          "Tomorrow");
    test_getRelativeTimeSpanString_helper(24 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "Yesterday",
                                          "Tomorrow");
    test_getRelativeTimeSpanString_helper(2 * DAY_IN_MILLIS, DAY_IN_MILLIS, "2 days ago",
                                          "in 2 days");
    test_getRelativeTimeSpanString_helper(25 * DAY_IN_MILLIS, DAY_IN_MILLIS, "January 11",
                                          "March 2");

    test_getRelativeTimeSpanString_helper(0 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "0 weeks ago",
                                          "0 weeks ago");
    test_getRelativeTimeSpanString_helper(1 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "1 week ago",
                                          "in 1 week");
    test_getRelativeTimeSpanString_helper(2 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "2 weeks ago",
                                          "in 2 weeks");
    test_getRelativeTimeSpanString_helper(25 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, "25 weeks ago",
                                          "in 25 weeks");

    // duration >= minResolution
    test_getRelativeTimeSpanString_helper(30 * SECOND_IN_MILLIS, 0, "30 seconds ago",
                                          "in 30 seconds");
    test_getRelativeTimeSpanString_helper(30 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS,
                                          "30 minutes ago", "in 30 minutes");
    test_getRelativeTimeSpanString_helper(30 * HOUR_IN_MILLIS, MINUTE_IN_MILLIS, "Yesterday",
                                          "Tomorrow");
    test_getRelativeTimeSpanString_helper(5 * DAY_IN_MILLIS, MINUTE_IN_MILLIS, "5 days ago",
                                          "in 5 days");
    test_getRelativeTimeSpanString_helper(30 * WEEK_IN_MILLIS, MINUTE_IN_MILLIS, "July 10, 2014",
                                          "September 3");
    test_getRelativeTimeSpanString_helper(5 * 365 * DAY_IN_MILLIS, MINUTE_IN_MILLIS,
                                          "February 6, 2010", "February 4, 2020");

    test_getRelativeTimeSpanString_helper(60 * SECOND_IN_MILLIS, MINUTE_IN_MILLIS, "1 minute ago",
                                          "in 1 minute");
    test_getRelativeTimeSpanString_helper(120 * SECOND_IN_MILLIS - 1, MINUTE_IN_MILLIS,
                                          "1 minute ago", "in 1 minute");
    test_getRelativeTimeSpanString_helper(60 * MINUTE_IN_MILLIS, HOUR_IN_MILLIS, "1 hour ago",
                                          "in 1 hour");
    test_getRelativeTimeSpanString_helper(120 * MINUTE_IN_MILLIS - 1, HOUR_IN_MILLIS, "1 hour ago",
                                          "in 1 hour");
    test_getRelativeTimeSpanString_helper(2 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "Today", "Today");
    test_getRelativeTimeSpanString_helper(12 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "Yesterday",
                                          "Today");
    test_getRelativeTimeSpanString_helper(24 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "Yesterday",
                                          "Tomorrow");
    test_getRelativeTimeSpanString_helper(48 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "2 days ago",
                                          "in 2 days");
    test_getRelativeTimeSpanString_helper(45 * HOUR_IN_MILLIS, DAY_IN_MILLIS, "2 days ago",
                                          "in 2 days");
    test_getRelativeTimeSpanString_helper(7 * DAY_IN_MILLIS, WEEK_IN_MILLIS, "1 week ago",
                                          "in 1 week");
    test_getRelativeTimeSpanString_helper(14 * DAY_IN_MILLIS - 1, WEEK_IN_MILLIS, "1 week ago",
                                          "in 1 week");

    // duration < minResolution
    test_getRelativeTimeSpanString_helper(59 * SECOND_IN_MILLIS, MINUTE_IN_MILLIS, "0 minutes ago",
                                          "in 0 minutes");
    test_getRelativeTimeSpanString_helper(59 * MINUTE_IN_MILLIS, HOUR_IN_MILLIS, "0 hours ago",
                                          "in 0 hours");
    test_getRelativeTimeSpanString_helper(HOUR_IN_MILLIS - 1, HOUR_IN_MILLIS, "0 hours ago",
                                          "in 0 hours");
    test_getRelativeTimeSpanString_helper(DAY_IN_MILLIS - 1, DAY_IN_MILLIS, "Yesterday",
                                          "Tomorrow");
    test_getRelativeTimeSpanString_helper(20 * SECOND_IN_MILLIS, WEEK_IN_MILLIS, "0 weeks ago",
                                          "in 0 weeks");
    test_getRelativeTimeSpanString_helper(WEEK_IN_MILLIS - 1, WEEK_IN_MILLIS, "0 weeks ago",
                                          "in 0 weeks");
  }

  public void test_getRelativeTimeSpanStringAbbrev() throws Exception {
    int flags = FORMAT_ABBREV_RELATIVE;

    test_getRelativeTimeSpanString_helper(0 * SECOND_IN_MILLIS, 0, flags, "0 sec. ago",
                                          "0 sec. ago");
    test_getRelativeTimeSpanString_helper(1 * MINUTE_IN_MILLIS, 0, flags, "1 min. ago",
                                          "in 1 min.");
    test_getRelativeTimeSpanString_helper(5 * DAY_IN_MILLIS, 0, flags, "5 days ago", "in 5 days");

    test_getRelativeTimeSpanString_helper(0 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, flags,
                                          "0 sec. ago", "0 sec. ago");
    test_getRelativeTimeSpanString_helper(1 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, flags,
                                          "1 sec. ago", "in 1 sec.");
    test_getRelativeTimeSpanString_helper(2 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, flags,
                                          "2 sec. ago", "in 2 sec.");
    test_getRelativeTimeSpanString_helper(25 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, flags,
                                          "25 sec. ago", "in 25 sec.");
    test_getRelativeTimeSpanString_helper(75 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, flags,
                                          "1 min. ago", "in 1 min.");
    test_getRelativeTimeSpanString_helper(5000 * SECOND_IN_MILLIS, SECOND_IN_MILLIS, flags,
                                          "1 hr. ago", "in 1 hr.");

    test_getRelativeTimeSpanString_helper(0 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "0 min. ago", "0 min. ago");
    test_getRelativeTimeSpanString_helper(1 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "1 min. ago", "in 1 min.");
    test_getRelativeTimeSpanString_helper(2 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "2 min. ago", "in 2 min.");
    test_getRelativeTimeSpanString_helper(25 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "25 min. ago", "in 25 min.");
    test_getRelativeTimeSpanString_helper(75 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "1 hr. ago", "in 1 hr.");
    test_getRelativeTimeSpanString_helper(720 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "12 hr. ago", "in 12 hr.");

    test_getRelativeTimeSpanString_helper(0 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, flags,
                                          "0 hr. ago", "0 hr. ago");
    test_getRelativeTimeSpanString_helper(1 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, flags,
                                          "1 hr. ago", "in 1 hr.");
    test_getRelativeTimeSpanString_helper(2 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, flags,
                                          "2 hr. ago", "in 2 hr.");
    test_getRelativeTimeSpanString_helper(5 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, flags,
                                          "5 hr. ago", "in 5 hr.");
    test_getRelativeTimeSpanString_helper(20 * HOUR_IN_MILLIS, HOUR_IN_MILLIS, flags,
                                          "20 hr. ago", "in 20 hr.");

    test_getRelativeTimeSpanString_helper(0 * DAY_IN_MILLIS, DAY_IN_MILLIS, flags, "Today",
                                          "Today");
    test_getRelativeTimeSpanString_helper(20 * HOUR_IN_MILLIS, DAY_IN_MILLIS, flags,
                                          "Yesterday", "Tomorrow");
    test_getRelativeTimeSpanString_helper(24 * HOUR_IN_MILLIS, DAY_IN_MILLIS, flags,
                                          "Yesterday", "Tomorrow");
    test_getRelativeTimeSpanString_helper(2 * DAY_IN_MILLIS, DAY_IN_MILLIS, flags,
                                          "2 days ago", "in 2 days");
    test_getRelativeTimeSpanString_helper(25 * DAY_IN_MILLIS, DAY_IN_MILLIS, flags,
                                          "January 11", "March 2");

    test_getRelativeTimeSpanString_helper(0 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, flags,
                                          "0 wk. ago", "0 wk. ago");
    test_getRelativeTimeSpanString_helper(1 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, flags,
                                          "1 wk. ago", "in 1 wk.");
    test_getRelativeTimeSpanString_helper(2 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, flags,
                                          "2 wk. ago", "in 2 wk.");
    test_getRelativeTimeSpanString_helper(25 * WEEK_IN_MILLIS, WEEK_IN_MILLIS, flags,
                                          "25 wk. ago", "in 25 wk.");

    // duration >= minResolution
    test_getRelativeTimeSpanString_helper(30 * SECOND_IN_MILLIS, 0, flags, "30 sec. ago",
                                          "in 30 sec.");
    test_getRelativeTimeSpanString_helper(30 * MINUTE_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "30 min. ago", "in 30 min.");
    test_getRelativeTimeSpanString_helper(30 * HOUR_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "Yesterday", "Tomorrow");
    test_getRelativeTimeSpanString_helper(5 * DAY_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "5 days ago", "in 5 days");
    test_getRelativeTimeSpanString_helper(30 * WEEK_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "July 10, 2014", "September 3");
    test_getRelativeTimeSpanString_helper(5 * 365 * DAY_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "February 6, 2010", "February 4, 2020");

    test_getRelativeTimeSpanString_helper(60 * SECOND_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "1 min. ago", "in 1 min.");
    test_getRelativeTimeSpanString_helper(120 * SECOND_IN_MILLIS - 1, MINUTE_IN_MILLIS, flags,
                                          "1 min. ago", "in 1 min.");
    test_getRelativeTimeSpanString_helper(60 * MINUTE_IN_MILLIS, HOUR_IN_MILLIS, flags,
                                          "1 hr. ago", "in 1 hr.");
    test_getRelativeTimeSpanString_helper(120 * MINUTE_IN_MILLIS - 1, HOUR_IN_MILLIS, flags,
                                          "1 hr. ago", "in 1 hr.");
    test_getRelativeTimeSpanString_helper(2 * HOUR_IN_MILLIS, DAY_IN_MILLIS, flags, "Today",
                                          "Today");
    test_getRelativeTimeSpanString_helper(12 * HOUR_IN_MILLIS, DAY_IN_MILLIS, flags,
                                          "Yesterday", "Today");
    test_getRelativeTimeSpanString_helper(24 * HOUR_IN_MILLIS, DAY_IN_MILLIS, flags,
                                          "Yesterday", "Tomorrow");
    test_getRelativeTimeSpanString_helper(48 * HOUR_IN_MILLIS, DAY_IN_MILLIS, flags,
                                          "2 days ago", "in 2 days");
    test_getRelativeTimeSpanString_helper(45 * HOUR_IN_MILLIS, DAY_IN_MILLIS, flags,
                                          "2 days ago", "in 2 days");
    test_getRelativeTimeSpanString_helper(7 * DAY_IN_MILLIS, WEEK_IN_MILLIS, flags,
                                          "1 wk. ago", "in 1 wk.");
    test_getRelativeTimeSpanString_helper(14 * DAY_IN_MILLIS - 1, WEEK_IN_MILLIS, flags,
                                          "1 wk. ago", "in 1 wk.");

    // duration < minResolution
    test_getRelativeTimeSpanString_helper(59 * SECOND_IN_MILLIS, MINUTE_IN_MILLIS, flags,
                                          "0 min. ago", "in 0 min.");
    test_getRelativeTimeSpanString_helper(59 * MINUTE_IN_MILLIS, HOUR_IN_MILLIS, flags,
                                          "0 hr. ago", "in 0 hr.");
    test_getRelativeTimeSpanString_helper(HOUR_IN_MILLIS - 1, HOUR_IN_MILLIS, flags,
                                          "0 hr. ago", "in 0 hr.");
    test_getRelativeTimeSpanString_helper(DAY_IN_MILLIS - 1, DAY_IN_MILLIS, flags,
                                          "Yesterday", "Tomorrow");
    test_getRelativeTimeSpanString_helper(20 * SECOND_IN_MILLIS, WEEK_IN_MILLIS, flags,
                                          "0 wk. ago", "in 0 wk.");
    test_getRelativeTimeSpanString_helper(WEEK_IN_MILLIS - 1, WEEK_IN_MILLIS, flags,
                                          "0 wk. ago", "in 0 wk.");

  }

  public void test_getRelativeTimeSpanStringGerman() throws Exception {
    Locale de_DE = new Locale("de", "DE");
    final long now = System.currentTimeMillis();
    TimeZone tz = TimeZone.getDefault();

    // 42 minutes ago
    assertEquals("vor 42 Minuten",
      getRelativeTimeSpanString(de_DE, tz, now - 42 * MINUTE_IN_MILLIS, now,
                                MINUTE_IN_MILLIS, 0));
    // in 42 minutes
    assertEquals("in 42 Minuten",
      getRelativeTimeSpanString(de_DE, tz, now + 42 * MINUTE_IN_MILLIS, now,
                                MINUTE_IN_MILLIS, 0));
    // yesterday
    assertEquals("Gestern",
                 getRelativeTimeSpanString(de_DE, tz, now - DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // the day before yesterday
    assertEquals("Vorgestern",
                 getRelativeTimeSpanString(de_DE, tz, now - 2 * DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // tomorrow
    assertEquals("Morgen",
                 getRelativeTimeSpanString(de_DE, tz, now + DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // the day after tomorrow
    assertEquals("Übermorgen",
                 getRelativeTimeSpanString(de_DE, tz, now + 2 * DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
  }

  public void test_getRelativeTimeSpanStringFrench() throws Exception {
    Locale fr_FR = new Locale("fr", "FR");
    final long now = System.currentTimeMillis();
    TimeZone tz = TimeZone.getDefault();

    // 42 minutes ago
    assertEquals("il y a 42 minutes",
                 getRelativeTimeSpanString(fr_FR, tz, now - (42 * MINUTE_IN_MILLIS), now,
                                           MINUTE_IN_MILLIS, 0));
    // in 42 minutes
    assertEquals("dans 42 minutes",
                 getRelativeTimeSpanString(fr_FR, tz, now + (42 * MINUTE_IN_MILLIS), now,
                                           MINUTE_IN_MILLIS, 0));
    // yesterday
    assertEquals("Hier",
                 getRelativeTimeSpanString(fr_FR, tz, now - DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // the day before yesterday
    assertEquals("Avant-hier",
                 getRelativeTimeSpanString(fr_FR, tz, now - 2 * DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // tomorrow
    assertEquals("Demain",
                 getRelativeTimeSpanString(fr_FR, tz, now + DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
    // the day after tomorrow
    assertEquals("Après-demain",
                 getRelativeTimeSpanString(fr_FR, tz, now + 2 * DAY_IN_MILLIS, now,
                                           DAY_IN_MILLIS, 0));
  }

  // Tests adopted from CTS tests for DateUtils.getRelativeDateTimeString.
  public void test_getRelativeDateTimeStringCTS() throws Exception {
    Locale en_US = Locale.getDefault();
    TimeZone tz = TimeZone.getDefault();
    final long baseTime = System.currentTimeMillis();

    final long DAY_DURATION = 5 * 24 * 60 * 60 * 1000;
    assertNotNull(getRelativeDateTimeString(en_US, tz, baseTime - DAY_DURATION, baseTime,
                                            MINUTE_IN_MILLIS, DAY_IN_MILLIS,
                                            FORMAT_NUMERIC_DATE));
  }

  public void test_getRelativeDateTimeString() throws Exception {
    Locale en_US = new Locale("en", "US");
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    Calendar cal = Calendar.getInstance(tz, en_US);
    // Feb 5, 2015 at 10:50 PST
    cal.set(2015, Calendar.FEBRUARY, 5, 10, 50, 0);
    final long base = cal.getTimeInMillis();

    assertEquals("5 seconds ago, 10:49 AM",
                 getRelativeDateTimeString(en_US, tz, base - 5 * SECOND_IN_MILLIS, base, 0,
                                           MINUTE_IN_MILLIS, 0));
    assertEquals("5 min. ago, 10:45 AM",
                 getRelativeDateTimeString(en_US, tz, base - 5 * MINUTE_IN_MILLIS, base, 0,
                                           HOUR_IN_MILLIS, FORMAT_ABBREV_RELATIVE));
    assertEquals("0 hr. ago, 10:45 AM",
                 getRelativeDateTimeString(en_US, tz, base - 5 * MINUTE_IN_MILLIS, base,
                                           HOUR_IN_MILLIS, DAY_IN_MILLIS, FORMAT_ABBREV_RELATIVE));
    assertEquals("5 hours ago, 5:50 AM",
                 getRelativeDateTimeString(en_US, tz, base - 5 * HOUR_IN_MILLIS, base,
                                           HOUR_IN_MILLIS, DAY_IN_MILLIS, 0));
    assertEquals("Yesterday, 7:50 PM",
                 getRelativeDateTimeString(en_US, tz, base - 15 * HOUR_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, FORMAT_ABBREV_RELATIVE));
    assertEquals("5 days ago, 10:50 AM",
                 getRelativeDateTimeString(en_US, tz, base - 5 * DAY_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, 0));
    assertEquals("Jan 29, 10:50 AM",
                 getRelativeDateTimeString(en_US, tz, base - 7 * DAY_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, 0));
    assertEquals("11/27/2014, 10:50 AM",
                 getRelativeDateTimeString(en_US, tz, base - 10 * WEEK_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, 0));
    assertEquals("11/27/2014, 10:50 AM",
                 getRelativeDateTimeString(en_US, tz, base - 10 * WEEK_IN_MILLIS, base, 0,
                                           YEAR_IN_MILLIS, 0));

    // User-supplied flags should be ignored when formatting the date clause.
    final int FORMAT_SHOW_WEEKDAY = 0x00002;
    assertEquals("11/27/2014, 10:50 AM",
                 getRelativeDateTimeString(en_US, tz, base - 10 * WEEK_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS,
                                           FORMAT_ABBREV_ALL | FORMAT_SHOW_WEEKDAY));
  }

  public void test_getRelativeDateTimeStringDST() throws Exception {
    Locale en_US = new Locale("en", "US");
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    Calendar cal = Calendar.getInstance(tz, en_US);

    // DST starts on Mar 9, 2014 at 2:00 AM.
    // So 5 hours before 3:15 AM should be formatted as 'Yesterday, 9:15 PM'.
    cal.set(2014, Calendar.MARCH, 9, 3, 15, 0);
    long base = cal.getTimeInMillis();
    assertEquals("Yesterday, 9:15 PM",
                 getRelativeDateTimeString(en_US, tz, base - 5 * HOUR_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, 0));

    // 1 hour after 2:00 AM should be formatted as 'in 1 hour, 4:00 AM'.
    cal.set(2014, Calendar.MARCH, 9, 2, 0, 0);
    base = cal.getTimeInMillis();
    assertEquals("in 1 hour, 4:00 AM",
                 getRelativeDateTimeString(en_US, tz, base + 1 * HOUR_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, 0));

    // DST ends on Nov 2, 2014 at 2:00 AM. Clocks are turned backward 1 hour to
    // 1:00 AM. 8 hours before 5:20 AM should be 'Yesterday, 10:20 PM'.
    cal.set(2014, Calendar.NOVEMBER, 2, 5, 20, 0);
    base = cal.getTimeInMillis();
    assertEquals("Yesterday, 10:20 PM",
                 getRelativeDateTimeString(en_US, tz, base - 8 * HOUR_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, 0));

    cal.set(2014, Calendar.NOVEMBER, 2, 0, 45, 0);
    base = cal.getTimeInMillis();
    // 45 minutes after 0:45 AM should be 'in 45 minutes, 1:30 AM'.
    assertEquals("in 45 minutes, 1:30 AM",
                 getRelativeDateTimeString(en_US, tz, base + 45 * MINUTE_IN_MILLIS, base, 0,
                                           WEEK_IN_MILLIS, 0));
    // 45 minutes later, it should be 'in 45 minutes, 1:15 AM'.
    assertEquals("in 45 minutes, 1:15 AM",
                 getRelativeDateTimeString(en_US, tz, base + 90 * MINUTE_IN_MILLIS,
                                           base + 45 * MINUTE_IN_MILLIS, 0, WEEK_IN_MILLIS, 0));
    // Another 45 minutes later, it should be 'in 45 minutes, 2:00 AM'.
    assertEquals("in 45 minutes, 2:00 AM",
                 getRelativeDateTimeString(en_US, tz, base + 135 * MINUTE_IN_MILLIS,
                                           base + 90 * MINUTE_IN_MILLIS, 0, WEEK_IN_MILLIS, 0));
  }


  public void test_getRelativeDateTimeStringItalian() throws Exception {
    Locale it_IT = new Locale("it", "IT");
    TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
    Calendar cal = Calendar.getInstance(tz, it_IT);
    // 05 febbraio 2015 20:15
    cal.set(2015, Calendar.FEBRUARY, 5, 20, 15, 0);
    final long base = cal.getTimeInMillis();

    assertEquals("5 secondi fa, 20:14",
                 getRelativeDateTimeString(it_IT, tz, base - 5 * SECOND_IN_MILLIS, base, 0,
                                           MINUTE_IN_MILLIS, 0));
    assertEquals("5 min. fa, 20:10",
                 getRelativeDateTimeString(it_IT, tz, base - 5 * MINUTE_IN_MILLIS, base, 0,
                                           HOUR_IN_MILLIS, FORMAT_ABBREV_RELATIVE));
    assertEquals("0 h. fa, 20:10",
                 getRelativeDateTimeString(it_IT, tz, base - 5 * MINUTE_IN_MILLIS, base,
                                           HOUR_IN_MILLIS, DAY_IN_MILLIS, FORMAT_ABBREV_RELATIVE));
    assertEquals("Ieri, 22:15",
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
    Locale en_US = new Locale("en", "US");
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");

    // Now is Sep 2, 2011, 10:23 AM PDT.
    Calendar nowCalendar = Calendar.getInstance(tz, en_US);
    nowCalendar.set(2011, Calendar.SEPTEMBER, 2, 10, 23, 0);
    final long now = nowCalendar.getTimeInMillis();

    // Sep 1, 2011, 10:24 AM
    Calendar yesterdayCalendar1 = Calendar.getInstance(tz, en_US);
    yesterdayCalendar1.set(2011, Calendar.SEPTEMBER, 1, 10, 24, 0);
    long yesterday1 = yesterdayCalendar1.getTimeInMillis();
    assertEquals("Yesterday, 10:24 AM",
                 getRelativeDateTimeString(en_US, tz, yesterday1, now, MINUTE_IN_MILLIS,
                                           WEEK_IN_MILLIS, 0));

    // Sep 1, 2011, 10:22 AM
    Calendar yesterdayCalendar2 = Calendar.getInstance(tz, en_US);
    yesterdayCalendar2.set(2011, Calendar.SEPTEMBER, 1, 10, 22, 0);
    long yesterday2 = yesterdayCalendar2.getTimeInMillis();
    assertEquals("Yesterday, 10:22 AM",
                 getRelativeDateTimeString(en_US, tz, yesterday2, now, MINUTE_IN_MILLIS,
                                           WEEK_IN_MILLIS, 0));

    // Aug 31, 2011, 10:24 AM
    Calendar twoDaysAgoCalendar1 = Calendar.getInstance(tz, en_US);
    twoDaysAgoCalendar1.set(2011, Calendar.AUGUST, 31, 10, 24, 0);
    long twoDaysAgo1 = twoDaysAgoCalendar1.getTimeInMillis();
    assertEquals("2 days ago, 10:24 AM",
                 getRelativeDateTimeString(en_US, tz, twoDaysAgo1, now, MINUTE_IN_MILLIS,
                                           WEEK_IN_MILLIS, 0));

    // Aug 31, 2011, 10:22 AM
    Calendar twoDaysAgoCalendar2 = Calendar.getInstance(tz, en_US);
    twoDaysAgoCalendar2.set(2011, Calendar.AUGUST, 31, 10, 22, 0);
    long twoDaysAgo2 = twoDaysAgoCalendar2.getTimeInMillis();
    assertEquals("2 days ago, 10:22 AM",
                 getRelativeDateTimeString(en_US, tz, twoDaysAgo2, now, MINUTE_IN_MILLIS,
                                           WEEK_IN_MILLIS, 0));

    // Sep 3, 2011, 10:22 AM
    Calendar tomorrowCalendar1 = Calendar.getInstance(tz, en_US);
    tomorrowCalendar1.set(2011, Calendar.SEPTEMBER, 3, 10, 22, 0);
    long tomorrow1 = tomorrowCalendar1.getTimeInMillis();
    assertEquals("Tomorrow, 10:22 AM",
                 getRelativeDateTimeString(en_US, tz, tomorrow1, now, MINUTE_IN_MILLIS,
                                           WEEK_IN_MILLIS, 0));

    // Sep 3, 2011, 10:24 AM
    Calendar tomorrowCalendar2 = Calendar.getInstance(tz, en_US);
    tomorrowCalendar2.set(2011, Calendar.SEPTEMBER, 3, 10, 24, 0);
    long tomorrow2 = tomorrowCalendar2.getTimeInMillis();
    assertEquals("Tomorrow, 10:24 AM",
                 getRelativeDateTimeString(en_US, tz, tomorrow2, now, MINUTE_IN_MILLIS,
                                           WEEK_IN_MILLIS, 0));

    // Sep 4, 2011, 10:22 AM
    Calendar twoDaysLaterCalendar1 = Calendar.getInstance(tz, en_US);
    twoDaysLaterCalendar1.set(2011, Calendar.SEPTEMBER, 4, 10, 22, 0);
    long twoDaysLater1 = twoDaysLaterCalendar1.getTimeInMillis();
    assertEquals("in 2 days, 10:22 AM",
                 getRelativeDateTimeString(en_US, tz, twoDaysLater1, now, MINUTE_IN_MILLIS,
                                           WEEK_IN_MILLIS, 0));

    // Sep 4, 2011, 10:24 AM
    Calendar twoDaysLaterCalendar2 = Calendar.getInstance(tz, en_US);
    twoDaysLaterCalendar2.set(2011, Calendar.SEPTEMBER, 4, 10, 24, 0);
    long twoDaysLater2 = twoDaysLaterCalendar2.getTimeInMillis();
    assertEquals("in 2 days, 10:24 AM",
                 getRelativeDateTimeString(en_US, tz, twoDaysLater2, now, MINUTE_IN_MILLIS,
                                           WEEK_IN_MILLIS, 0));
  }
}
