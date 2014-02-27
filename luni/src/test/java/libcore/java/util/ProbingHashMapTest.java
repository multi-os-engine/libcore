/*
 * Copyright (C) 2014 Google Inc.
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

import java.util.OpenAddressingHashMap;
import junit.framework.TestCase;

public class ProbingHashMapTest extends TestCase {
    public void testInsert() {
        OpenAddressingHashMap<String, String> map = new OpenAddressingHashMap<String, String>();
        map.put("foo", "foo_value");
        map.put("bar", "bar_value");
        map.put("baz", "baz_value");
        map.put("bak", "bak_value");
        map.put("bak1", "bak1_value");
        map.put("bak2", "bak2_value");


        assertEquals("foo_value", map.get("foo"));
        assertEquals("bar_value", map.get("bar"));
    }
}
