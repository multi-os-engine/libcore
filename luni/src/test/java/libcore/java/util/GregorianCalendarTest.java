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


import junit.framework.TestCase;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class GregorianCalendarTest extends TestCase {

    // https://code.google.com/p/android/issues/detail?id=61993
    public void test_computeFields_dayOfWeekAndWeekOfYearSet() {
        // We can't use any of the public constructors because they have
        // the side effect of setting a month / day and wont trigger this bug
        // as a result.
        Calendar greg = new GregorianCalendar(100000);
        greg.set(Calendar.WEEK_OF_YEAR, 1);
        greg.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        assertEquals(1, greg.get(Calendar.WEEK_OF_YEAR));
        assertEquals(Calendar.MONDAY, greg.get(Calendar.DAY_OF_WEEK));
    }
}
