/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package util;

import java.util.ArrayList;
import java.util.List;

public class ListDefaultMethodTester extends junit.framework.TestCase {

    public static void test_replaceAll(List<Double> l) {
        l.add(5.0);
        l.add(2.0);
        l.add(-3.0);
        l.replaceAll(v -> v * 2);
        assertEquals(10.0, l.get(0));
        assertEquals(4.0, l.get(1));
        assertEquals(-6.0, l.get(2));

        try {
            l.replaceAll(null);
            fail();
        } catch (NullPointerException e) {
            // expected for null operator
        }
    }

    public static void test_sort(List<Double> l) {
        l.add(5.0);
        l.add(2.0);
        l.add(-3.0);
        l.sort((v1, v2) -> v1.compareTo(v2));
        assertEquals(-3.0, l.get(0));
        assertEquals(2.0, l.get(1));
        assertEquals(5.0, l.get(2));
    }
}
