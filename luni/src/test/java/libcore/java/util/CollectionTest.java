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
 * limitations under the License.
 */

package libcore.java.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

public class CollectionTest extends TestCase {

    private static final Predicate<Integer> isEven = x -> x % 2 == 0;
    private static final Predicate<Integer> isOdd = isEven.negate();
    private static final Predicate<Object> truth = x -> true;
    private static final Predicate<Object> falsity = x -> false;

    public void testRemoveIf() {

        ArrayList<Integer> integerList = new ArrayList<>();
        Collection<Integer> integerCollection = integerList;
        for (int i = 0; i < 100; ++i) {
            integerCollection.add(i);
        }

        integerCollection.removeIf(isEven);
        for (int i = 0; i < integerList.size(); i++) {
            assertTrue(integerList.get(i) % 2 != 0);
            if (i < integerList.size() - 1) {
                assertTrue(integerList.get(i + 1) > integerList.get(i));
            }
        }

        integerCollection.removeIf(isOdd);
        assertTrue(integerCollection.isEmpty());
    }

    public void testRemoveIfEmpty() {
        Collection<Integer> collection = new ArrayList<>();
        collection.removeIf(truth);
        assertTrue(collection.isEmpty());
    }

    public void testRemoveIfUnsupportedOperation() {
        Collection<Integer> c = Arrays.asList(1, 2, 3, 4);
        try {
            c.removeIf(truth);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        try {
            c.removeIf(falsity);
        } catch (Exception e) {
            fail();
        }
    }

    public void testRemoveIfNull() {
        try {
            Collection<Integer> c = new ArrayList<>();
            c.removeIf(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

}
