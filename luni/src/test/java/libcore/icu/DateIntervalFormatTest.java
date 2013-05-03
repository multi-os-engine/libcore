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

import java.util.Arrays;
import java.util.Locale;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static libcore.icu.DateIntervalFormat.*;

public class DateIntervalFormatTest extends junit.framework.TestCase {
  public static void assertEquals(String lhs, String rhs) {
    if (lhs.equals(rhs)) {
      System.err.println("PASS: " + lhs + " == " + rhs);
    } else {
      System.err.println("FAIL: " + lhs + " != " + rhs);
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

    // TODO: these are the current CTS tests for DateIntervalFormat.formatDateRange. note that not one of them tests an actual range!

    assertEquals("Monday", formatDateRange(fixedTime, fixedTime + HOUR_DURATION, FORMAT_SHOW_WEEKDAY));
    assertEquals("January 19", formatDateRange(timeWithCurrentYear, timeWithCurrentYear + HOUR_DURATION, FORMAT_SHOW_DATE));
    assertEquals("3:30 AM", formatDateRange(fixedTime, fixedTime, FORMAT_SHOW_TIME));
    assertEquals("January 19, 2009", formatDateRange(fixedTime, fixedTime + HOUR_DURATION, FORMAT_SHOW_YEAR));
    assertEquals("January 19", formatDateRange(timeWithCurrentYear, timeWithCurrentYear + HOUR_DURATION, FORMAT_NO_YEAR));
    assertEquals("January", formatDateRange(timeWithCurrentYear, timeWithCurrentYear + HOUR_DURATION, FORMAT_NO_MONTH_DAY));
    assertEquals("3:30 AM", formatDateRange(fixedTime, fixedTime, FORMAT_12HOUR | FORMAT_SHOW_TIME));
    assertEquals("03:30", formatDateRange(fixedTime, fixedTime, FORMAT_24HOUR | FORMAT_SHOW_TIME));
    assertEquals("3:30 AM", formatDateRange(fixedTime, fixedTime, FORMAT_12HOUR /*| FORMAT_CAP_AMPM*/ | FORMAT_SHOW_TIME));
    assertEquals("12:00 PM", formatDateRange(fixedTime + noonDuration, fixedTime + noonDuration, FORMAT_12HOUR | FORMAT_SHOW_TIME));
    assertEquals("12:00 PM", formatDateRange(fixedTime + noonDuration, fixedTime + noonDuration, FORMAT_12HOUR | FORMAT_SHOW_TIME /*| FORMAT_CAP_NOON*/));
    assertEquals("12:00 PM", formatDateRange(fixedTime + noonDuration, fixedTime + noonDuration, FORMAT_12HOUR /*| FORMAT_NO_NOON*/ | FORMAT_SHOW_TIME));
    assertEquals("12:00 AM", formatDateRange(fixedTime - midnightDuration, fixedTime - midnightDuration, FORMAT_12HOUR | FORMAT_SHOW_TIME /*| FORMAT_NO_MIDNIGHT*/));
    assertEquals("3:30 AM", formatDateRange(fixedTime, fixedTime, FORMAT_SHOW_TIME | FORMAT_UTC));
    assertEquals("3 AM", formatDateRange(fixedTime - integralDuration, fixedTime - integralDuration, FORMAT_SHOW_TIME | FORMAT_ABBREV_TIME));
    assertEquals("Mon", formatDateRange(fixedTime, fixedTime + HOUR_DURATION, FORMAT_SHOW_WEEKDAY | FORMAT_ABBREV_WEEKDAY));
    assertEquals("Jan 19", formatDateRange(timeWithCurrentYear, timeWithCurrentYear + HOUR_DURATION, FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH));
    assertEquals("Jan 19", formatDateRange(timeWithCurrentYear, timeWithCurrentYear + HOUR_DURATION, FORMAT_SHOW_DATE | FORMAT_ABBREV_ALL));

    String actual = formatDateRange(fixedTime, fixedTime + HOUR_DURATION, FORMAT_SHOW_YEAR | FORMAT_NUMERIC_DATE);
    assertEquals("1/19/2009", actual);
  }
}
