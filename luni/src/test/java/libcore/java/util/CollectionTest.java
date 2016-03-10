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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CollectionTest extends TestCase {

    private static final Predicate<Integer> isEven = x -> x % 2 == 0;
    private static final Predicate<Integer> isOdd = isEven.negate();
    private static final Predicate<Object> truth = x -> true;
    private static final Predicate<Object> falsity = x -> false;

    // Concrete implementations of Collection interface.
    private static final List<Supplier<Collection<Integer>>> collectionImpls = Arrays.asList(
            ArrayDeque<Integer>::new,
            ArrayList<Integer>::new,
            HashSet<Integer>::new,
            LinkedHashSet<Integer>::new,
            LinkedList<Integer>::new,
            Stack<Integer>::new,
            TreeSet<Integer>::new,
            Vector<Integer>::new
    );

    public void testRemoveIf() {
        for (Supplier<Collection<Integer>> impl : collectionImpls) {
            Collection<Integer> integers = impl.get();
            for (int h = 0; h < 100; ++h) {
                // Insert a bunch of random integers.
                integers.add((h >>> 2) ^ (h >>> 5) ^ (h >>> 11) ^ (h >>> 17));
            }

            integers.removeIf(isEven);
            for (Integer i : integers) {
                assertTrue(i % 2 != 0);
            }

            integers.removeIf(isOdd);
            assertTrue(integers.isEmpty());
        }
    }

    public void testRemoveIfEmpty() {
        for (Supplier<Collection<Integer>> impl : collectionImpls) {
            Collection<Integer> collection = impl.get();
            collection.removeIf(truth);
            assertTrue(collection.isEmpty());
        }
    }

    public void testRemoveIfUnsupportedOperation() {
        Collection<Integer> c = Arrays.asList(1, 2, 3, 4);
        try {
            c.removeIf(truth);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        c.removeIf(falsity); // Should not throw UnsupportedOperationException.
    }

    public void testRemoveIfNull() {
        for (Supplier<Collection<Integer>> impl : collectionImpls) {
            try {
                Collection<Integer> c = impl.get();
                c.removeIf(null);
                fail();
            } catch (NullPointerException expected) {
            }
        }
    }
}
