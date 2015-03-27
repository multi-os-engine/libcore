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

import junit.framework.TestCase;

import libcore.util.ZoneInfoDB;

/**
 * Tests to check ICU data integrity.
 */
public class IcuSanityTest extends TestCase {
    public void testTimeZoneDataVersion() throws Exception {
        String icu4cTzVersion = ICU.getTZDataVersion();
        String zoneInfoTzVersion = ZoneInfoDB.getInstance().getVersion();
        assertEquals(icu4cTzVersion, zoneInfoTzVersion);
    }
}
