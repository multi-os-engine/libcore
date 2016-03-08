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

package libcore.java.util;

import java.util.HashMap;
import java.util.Map;

public class MapTest extends junit.framework.TestCase  {

    public void test_getOrDefault() {
        Map<Integer, Integer> m = new HashMap<>();

        // Unmapped key
        assertEquals(-1, (int)m.getOrDefault(1, -1));

        // Mapped key
        m.put(1, 11);
        assertEquals(11, (int)m.getOrDefault(1, -1));
    }

    public void test_forEach() {
        Map<Integer, Double> m = new HashMap<>();
        Map<Integer, Double> replica = new HashMap<>();
        m.put(1, 10.0);
        m.put(2, 20.0);
        m.put(3, null);
        m.forEach(replica::put);
        assertEquals(10.0, replica.get(1));
        assertEquals(20.0, replica.get(2));
        assertNull(replica.get(3));

        // Null pointer exception for empty function
        try {
            m.forEach(null);
        } catch (NullPointerException e) {
            //expected
        }
    }

    public void test_replaceAll() {
        Map<Integer, Double> m = new HashMap<>();
        m.put(1, 10.0);
        m.put(2, 20.0);
        m.replaceAll((k, v) -> k+v);

        assertTrue(m.get(1) == 11.0);
        assertTrue(m.get(2) == 22.0);

        // When replacement function is null
        try {
            m.replaceAll(null);
        } catch (NullPointerException e) {
            //expected
        }

        // When replacement function returns null
        try {
            m.replaceAll((k, v) -> null);
        } catch (NullPointerException e) {
            //expected
        }
    }

    public void test_putIfAbsent() {
        Map<Integer, Double> m = new HashMap<>();

        // When key doesn't exist, it should return null
        assertNull(m.putIfAbsent(1, 1.0));
        // When key exist, it returns the last stored value
        assertEquals(1.0, m.putIfAbsent(1, 1.0));
        assertEquals(1.0, m.putIfAbsent(1, 2.0));
    }

    public void test_remove() {
        Map<Integer, Double> m = new HashMap<>();
        // unmapped key
        assertFalse(m.remove(1, 1.0));

        m.put(1, 1.0);
        // unmapped key with the wrong value
        assertFalse(m.remove(1, 2.0));
        // mapped key with the correct value
        assertTrue(m.remove(1, 1.0));
        assertFalse(m.containsKey(1));
    }

    public void test_replace() {
        Map<Integer, Double> m = new HashMap<>();

        // For unmapped key
        assertFalse(m.replace(1, 1.0, 2.0));

        // For mapped key
        m.put(1, 1.0);
        assertFalse(m.replace(1, 2.0, 2.0));
        assertTrue(m.replace(1, 1.0, 2.0));
        // verify if the value has been replaced
        assertEquals(2.0, m.getOrDefault(1, -1.0));

        m = new HashMap<>();

        // For unmapped key
        assertNull(m.replace(1, 1.0));

        // For a key mapped to null value
        m.put(1, null);
        assertNull(m.replace(1, 1.0));

        // For mapped key
        m.put(1, 1.0);
        assertEquals(1.0, m.replace(1, 2.0));
        assertEquals(2.0, m.getOrDefault(1, -1.0));
    }

    public void test_computeIfAbsent() {
        Map<Integer, Double> m = new HashMap<>();
        assertEquals(5.0, m.computeIfAbsent(1, (k) -> 5*(double)k));
        // re-running on already computed key
        assertEquals(5.0, m.computeIfAbsent(1, k -> 6*(double)k));
        assertEquals(5.0, m.getOrDefault(1, -1.0));

        // Checking for null
        m.put(2, null);
        assertEquals(10.0, m.computeIfAbsent(2, (k) -> 5*(double)k));
        // re-running on already computed key
        assertEquals(10.0, m.computeIfAbsent(2, k -> 6*(double)k));
        assertEquals(10.0, m.getOrDefault(2, -1.0));
    }

    public void test_computeIfPresent() {
        Map<Integer, Double> m = new HashMap<>();
        // Checking for an unmapped key
        assertNull(m.computeIfPresent(1, (k, v) -> 5*(double)k + v));

        // Checking for null key
        m.put(1, null);
        assertNull(m.computeIfPresent(1, (k, v) -> 5.0));

        // Checking for a mapped key
        m.put(1, 5.0);
        assertEquals(11.0, m.computeIfPresent(1, (k, v) -> 6*(double)k + v));
        assertEquals(11.0, m.getOrDefault(1, -1.0));

        // Checking when the remapping function returns null
        assertNull(m.computeIfPresent(1, (k, v) -> null));
        assertFalse(m.containsKey(1));
    }

    public void test_compute() {
        Map<Integer, Double> m = new HashMap<>();
        // Checking for an unmapped key
        m.put(1, 10.0);
        assertEquals(11.0, m.compute(1, (k, v) -> k + v));
        assertEquals(11.0, m.getOrDefault(1, -1.0));

        // Checking for null value
        m.put(1, null);
        assertEquals(10.0, m.compute(1, (k, v) -> 10.0));
        assertEquals(10.0, m.getOrDefault(1, -1.0));

        // Checking when the remapping function returns null
        assertNull(m.compute(1, (k, v) -> null));
        assertFalse(m.containsKey(1));
    }

    public void test_merge() {
        Map<Integer, Double> m = new HashMap<>();
        // Checking for an unmapped key
        m.put(1, 10.0);
        assertEquals(25.0, m.merge(1, 15.0, (v1, v2) -> v1 + v2));
        assertEquals(25.0, m.getOrDefault(1, -1.0));

        // For null value
        m.put(1, null);
        assertEquals(15.0, m.merge(1, 15.0, (v1, v2) -> v2));
        assertEquals(15.0, m.getOrDefault(1, -1.0));

        assertNull(m.merge(1, 2.0, (v1, v2) -> null));
        assertFalse(m.containsKey(1));
    }
}
