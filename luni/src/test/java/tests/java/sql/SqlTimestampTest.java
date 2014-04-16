/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.sql.Timestamp;

import junit.framework.TestCase;

public class SqlTimestampTest extends TestCase {

    public void testValueOf() {
        Timestamp t1 = Timestamp.valueOf("2001-12-31 21:45:57.123456789");
        assertEquals(2001, t1.getYear() + 1900);
        assertEquals(12, t1.getMonth() + 1);
        assertEquals(1, t1.getDay()); // day of weak...
        assertEquals(21, t1.getHours());
        assertEquals(45, t1.getMinutes());
        assertEquals(57, t1.getSeconds());
        assertEquals(123456789, t1.getNanos());

        Timestamp t2 = Timestamp.valueOf("2001-01-02 01:05:07.123");
        assertEquals(2001, t2.getYear() + 1900);
        assertEquals(1, t2.getMonth() + 1);
        assertEquals(2, t2.getDay());
        assertEquals(1, t2.getHours());
        assertEquals(5, t2.getMinutes());
        assertEquals(7, t2.getSeconds());
        assertEquals(123000000, t2.getNanos());

        Timestamp t3 = Timestamp.valueOf("2001-03-04 01:05:07");
        assertEquals(2001, t3.getYear() + 1900);
        assertEquals(3, t3.getMonth() + 1);
        assertEquals(0, t3.getDay());
        assertEquals(1, t3.getHours());
        assertEquals(5, t3.getMinutes());
        assertEquals(7, t3.getSeconds());
        assertEquals(0, t3.getNanos());
    }

    public void testValueOfInvalidTimestamp() {
        String[] invalidDates = {
            "",
            "+2001-12-31", "2001-+12-31", "2001-12-+31",
            "-2001-12-31", "2001--12-31", "2001-12--31",
            "2001--","2001--31","-12-31", "-12-", "--31",
            "2001-12-31 21:45:57.+12345678", "2001-12-31 21:45:57.-12345678",
            "2001-12-31 21:45:57.1234567891"
        };

        for (String timestamp : invalidDates) {
            try {
                Timestamp.valueOf(timestamp);
                fail();
            } catch (IllegalArgumentException expected) { }
        }
    }

}
