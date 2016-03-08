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

public class HashMapTest extends junit.framework.TestCase {

  public void test_getOrDefault() {
    MapDefaultMethodTester.test_getOrDefault(new HashMap<>(), true);
  }

  public void test_forEach() {
    MapDefaultMethodTester.test_forEach(new HashMap<>());
  }

  public void test_putIfAbsent() {
    MapDefaultMethodTester.test_putIfAbsent(new HashMap<>());
  }

  public void test_remove() {
    MapDefaultMethodTester.test_remove(new HashMap<>());
  }

  public void test_replace$K$V$V() {
    MapDefaultMethodTester.test_replace$K$V$V(new HashMap<>());
  }

  public void test_replace$K$V() {
    MapDefaultMethodTester.test_replace$K$V(new HashMap<>(), true);
  }

  public void test_computeIfAbsent() {
    MapDefaultMethodTester.test_computeIfAbsent(new HashMap<>(), true);
  }

  public void test_computeIfPresent() {
    MapDefaultMethodTester.test_computeIfPresent(new HashMap<>(), true);
  }

  public void test_compute() {
    MapDefaultMethodTester.test_compute(new HashMap<>(), true);
  }

  public void test_merge() {
    MapDefaultMethodTester.test_merge(new HashMap<>(), true);
  }
}
