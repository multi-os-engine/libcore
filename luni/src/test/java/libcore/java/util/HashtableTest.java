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

import java.util.Hashtable;
import java.util.Map;

public class HashtableTest extends junit.framework.TestCase {

  public void test_getOrDefault() {
    MapDefaultMethodTester.test_getOrDefault(new Hashtable<>(), false);
    Hashtable<Integer, Double> m = new Hashtable<>();
    // For null key
    try {
      m.getOrDefault(null, -1.0);
      fail();
    } catch (NullPointerException e) {
      // expected
    }
  }

  public void test_forEach() {
    MapDefaultMethodTester.test_forEach(new Hashtable<>());

    // Null pointer exception for empty function
    Map<Integer, Double> m = new Hashtable<>();
    try {
      m.forEach(null);
    } catch (NullPointerException e) {
      //expected
    }
  }

  public void test_putIfAbsent() {
    MapDefaultMethodTester.test_putIfAbsent(new Hashtable<>());

    // Null pointer exception for empty function
    Map<Integer, Double> m = new Hashtable<>();
    try {
      m.putIfAbsent(null, 1.0);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }

    try {
      m.putIfAbsent(5, null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }

  public void test_remove() {
    MapDefaultMethodTester.test_remove(new Hashtable<>());

    Map<Integer, Double> m = new Hashtable<>();
    try {
      m.remove(null, 2.0);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }

  public void test_replace$K$V$V() {
    MapDefaultMethodTester.test_replace$K$V$V(new Hashtable<>());

    Hashtable<Integer, Double> m = new Hashtable<>();
    // For null key
    try {
      m.replace(null, 1.0, 5.0);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }

    // For null value
    m.put(1, 5.0);
    try {
      m.replace(1, 5.0, null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }

  public void test_replace$K$V() {
    MapDefaultMethodTester.test_replace$K$V(new Hashtable<>(), false);

    Hashtable<Integer, Double> m = new Hashtable<>();

    m.put(1, 1.0);
    // For replacing with a null value
    try {
      m.replace(1, null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }

    // For null key
    try {
      m.replace(null, 5.0);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }

  public void test_computeIfAbsent() {
    MapDefaultMethodTester.test_computeIfAbsent(new Hashtable<>(), false);

    Map<Integer, Double> m = new Hashtable<>();

    try {
      m.computeIfAbsent(10, null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }

    try {
      m.computeIfAbsent(null, k -> 6.0 * k);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }

  public void test_computeIfPresent() {
    MapDefaultMethodTester.test_computeIfPresent(new Hashtable<>(), false);
    Map<Integer, Double> m = new Hashtable<>();
    try {
      m.compute(null, (k, v) -> 10.0);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }

    try {
      m.compute(1, null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }

  public void test_compute() {
    MapDefaultMethodTester.test_compute(new Hashtable<>(), false);

    Map<Integer, Double> m = new Hashtable<>();
    try {
      m.compute(null, (k, v) -> 10.0);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }

    try {
      m.compute(1, null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }

  public void test_merge() {
    Map<Integer, Double> m = new Hashtable<>();
    // Checking for an unmapped key
    m.put(1, 10.0);
    assertEquals(25.0, m.merge(1, 15.0, (v1, v2) -> v1 + v2));
    assertEquals(25.0, m.getOrDefault(1, -1.0));

    assertNull(m.merge(1, 2.0, (v1, v2) -> null));
    assertFalse(m.containsKey(1));

    try {
      m.merge(null, 2.0, (v1, v2) -> null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }
}
