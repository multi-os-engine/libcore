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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNull;

public class MapDefaultMethodTester {

  public static void test_getOrDefault(Map<Integer, Double> m, boolean acceptsNullKey) {
    // Unmapped key
    assertEquals(-1.0, m.getOrDefault(1, -1.0));

    // Mapped key
    m.put(1, 11.0);
    assertEquals(11.0, m.getOrDefault(1, -1.0));

    // Null key
    if (acceptsNullKey) {
      m.put(1, null);
      assertEquals(null, m.getOrDefault(1, -1.0));
    }
  }

  public static void test_forEach(Map<Integer, Double> m) {
    Map<Integer, Double> replica = new HashMap<>();
    m.put(1, 10.0);
    m.put(2, 20.0);
    m.forEach(replica::put);
    assertEquals(10.0, replica.get(1));
    assertEquals(20.0, replica.get(2));

    // Null pointer exception for empty function
    try {
      m.forEach(null);
    } catch (NullPointerException expected) {
    }
  }

  public static void test_putIfAbsent(Map<Integer, Double> m) {
    // When key doesn't exist, it should return null
    assertNull(m.putIfAbsent(1, 1.0));
    // When key exist, it returns the last stored value
    assertEquals(1.0, m.putIfAbsent(1, 1.0));
    assertEquals(1.0, m.putIfAbsent(1, 2.0));
  }

  public static void test_remove(Map<Integer, Double> m) {
    // unmapped key
    assertFalse(m.remove(1, 1.0));

    // unmapped key with the wrong value
    m.put(1, 1.0);
    assertFalse(m.remove(1, 2.0));

    // mapped key with the correct value
    assertTrue(m.remove(1, 1.0));
    assertFalse(m.containsKey(1));
  }

  public static void test_replace$K$V$V(Map<Integer, Double> m) {
    // For unmapped key
    assertFalse(m.replace(1, 1.0, 2.0));

    // For mapped key
    m.put(1, 1.0);
    assertFalse(m.replace(1, 2.0, 2.0));
    assertTrue(m.replace(1, 1.0, 2.0));
    // verify if the value has been replaced
    assertEquals(2.0, m.getOrDefault(1, -1.0));
  }

  public static void test_replace$K$V(Map<Integer, Double> m, boolean acceptsNullKey) {
    // For unmapped key
    assertNull(m.replace(1, 1.0));

    if (acceptsNullKey){
      // For a key mapped to null value
      m.put(1, null);
      assertNull(m.replace(1, 1.0));
    }

    // For mapped key
    m.put(1, 1.0);
    assertEquals(1.0, m.replace(1, 2.0));
    assertEquals(2.0, m.getOrDefault(1, -1.0));
  }

  public static void test_computeIfAbsent(Map<Integer, Double> m, boolean acceptsNullKey) {
    assertEquals(5.0, m.computeIfAbsent(1, (k) -> 5.0 * k));
    // re-running on already computed key
    assertEquals(5.0, m.computeIfAbsent(1, k -> 6.0 * k));
    assertEquals(5.0, m.getOrDefault(1, -1.0));

    // Checking for null
    if (acceptsNullKey) {
      m.put(2, null);
      assertEquals(10.0, m.computeIfAbsent(2, (k) -> 5.0 * k));
    }
  }

  public static void test_computeIfPresent(Map<Integer, Double> m, boolean acceptsNullKey) {
    // Checking for an unmapped key
    assertNull(m.computeIfPresent(1, (k, v) -> 5.0 * k + v));

    if (acceptsNullKey) {
      // Checking for null key
      m.put(1, null);
      assertNull(m.computeIfPresent(1, (k, v) -> 5.0));
    }

    // Checking for a mapped key
    m.put(1, 5.0);
    assertEquals(11.0, m.computeIfPresent(1, (k, v) -> 6.0 * k + v));
    assertEquals(11.0, m.getOrDefault(1, -1.0));

    // Checking when the remapping function returns null
    assertNull(m.computeIfPresent(1, (k, v) -> null));
    assertFalse(m.containsKey(1));
  }

  public static void test_compute(Map<Integer, Double> m, boolean acceptsNullKey) {
    // Checking for an unmapped key
    m.put(1, 10.0);
    assertEquals(11.0, m.compute(1, (k, v) -> k + v));
    assertEquals(11.0, m.getOrDefault(1, -1.0));

    if (acceptsNullKey) {
      // Checking for null value
      m.put(1, null);
      assertEquals(10.0, m.compute(1, (k, v) -> 10.0));
      assertEquals(10.0, m.getOrDefault(1, -1.0));
    }

    // Checking when the remapping function returns null
    assertNull(m.compute(1, (k, v) -> null));
    assertFalse(m.containsKey(1));
  }

  public static void test_merge(Map<Integer, Double> m, boolean acceptsNullKey) {
    // Checking for an unmapped key
    m.put(1, 10.0);
    assertEquals(25.0, m.merge(1, 15.0, (v1, v2) -> v1 + v2));
    assertEquals(25.0, m.getOrDefault(1, -1.0));

    if (acceptsNullKey) {
      // For null value
      m.put(1, null);
      assertEquals(15.0, m.merge(1, 15.0, (v1, v2) -> v2));
      assertEquals(15.0, m.getOrDefault(1, -1.0));
    }

    assertNull(m.merge(1, 2.0, (v1, v2) -> null));
    assertFalse(m.containsKey(1));
  }
}
