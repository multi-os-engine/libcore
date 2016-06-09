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

package libcore.java.nio.file;

import junit.framework.TestCase;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class PathsTest extends TestCase {

    public void test_get_String() {
        assertEquals("d1", Paths.get("d1").toString());
        assertEquals("", Paths.get("").toString());
        assertEquals("/", Paths.get("//").toString());
        assertEquals("d1/d2/d3", Paths.get("d1//d2/d3").toString());
        assertEquals("d1/d2", Paths.get("d1", "", "d2").toString());

        try {
            Paths.get("'\u0000'");
            fail();
        } catch (InvalidPathException expected) {}
    }

    public void test_get_URI() throws URISyntaxException {
        assertEquals("/d1", Paths.get(new URI("file:///d1")).toString());
        assertEquals("/", Paths.get(new URI("file:///")).toString());
        // Redundant slashes exist in the output.
        assertEquals("/d1//d2/d3", Paths.get(new URI("file:///d1//d2/d3")).toString());

        try {
            Paths.get(new URI("d1"));
            fail();
        } catch (IllegalArgumentException expected) {}
    }
}
