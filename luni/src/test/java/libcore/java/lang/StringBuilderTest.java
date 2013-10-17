/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.java.lang;

public class StringBuilderTest extends junit.framework.TestCase {
    // See https://code.google.com/p/android/issues/detail?id=60639
    public void testDeleteLastChar() {
        StringBuilder sb = new StringBuilder("oarFish_");
        sb.append('a');
        String oarFishA = sb.toString();

        sb.deleteCharAt(sb.length() - 1);
        sb.append('b');
        String oarFishB = sb.toString();

        assertEquals("oarFish_a", oarFishA);
        assertEquals("oarFish_b", oarFishB);
    }

    // See https://code.google.com/p/android/issues/detail?id=60639
    public void testDeleteLastChar_singleChar() {
        StringBuilder sb = new StringBuilder();
        sb.append('a');
        String a = sb.toString();

        sb.deleteCharAt(0);
        sb.append('b');
        String b = sb.toString();

        assertEquals("a", a);
        assertEquals("b", b);
    }

    public void testDeleteRange_endsAtLastChar() {
        StringBuilder sb = new StringBuilder("newGuineaSinging");
        sb.append("Dog");
        String dog = sb.toString();

        sb.delete(sb.length() - 3, sb.length());
        sb.append("Cat");
        String cat = sb.toString();

        assertEquals("newGuineaSingingDog", dog);
        assertEquals("newGuineaSingingCat", cat);
    }

    public void testDeleteCharAt_boundChecks() {
        StringBuilder sb = new StringBuilder("yeti");

        try {
            sb.deleteCharAt(4);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {}

        try {
            sb.deleteCharAt(-1);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {}
    }

    public void testDelete_boundChecks() {
        StringBuilder sb = new StringBuilder("yeti");

        try {
            sb.delete(5, 6);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }

        try {
            sb.delete(4, 5);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }
}
