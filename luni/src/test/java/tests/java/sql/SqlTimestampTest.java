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
    String[] timestamps = {
      "2001-12-31 21:45:57.123456789", "2001-12-01 21:45:57.1", "2001-12-01 21:45:57"
    };

    for (String timestamp : timestamps) {
      Timestamp.valueOf(timestamp);
    }
  }

  public void testValueOfInvalidTimestamp() {
    String[] invalidDates = {
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
