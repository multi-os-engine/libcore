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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

public class SpliteratorTester {
    public static void testCharacteristics(Spliterator<?> spliterator,
                                           int[] expectedCharacteristics) {
        int charecteristicsMask = 0;
        for (int charecteristic : expectedCharacteristics) {
            assertTrue(spliterator.hasCharacteristics(charecteristic));
            charecteristicsMask |= charecteristic;
        }

        assertEquals(charecteristicsMask, spliterator.characteristics());
    }

    public static <T> void runBasicIterationTests(Spliterator<T> spliterator,
                                                  ArrayList<T> expectedElements) {
        ArrayList<T> recorder = new ArrayList<T>(expectedElements.size());
        Consumer<T> consumer = (T value) -> recorder.add(value);

        // tryAdvance.
        assertTrue(spliterator.tryAdvance(consumer));
        assertEquals(expectedElements.get(0), recorder.get(0));

        // forEachRemaining.
        spliterator.forEachRemaining(consumer);
        assertEquals(expectedElements, recorder);

        // There should be no more elements remaining in this spliterator.
        assertFalse(spliterator.tryAdvance(consumer));
        spliterator.forEachRemaining((T) -> fail());
    }

    public static <T> void runBasicIterationTests_unordered(Spliterator<T> spliterator,
                                                  ArrayList<T> expectedElements,
                                                  Comparator<T> comparator) {
        ArrayList<T> recorder = new ArrayList<T>(expectedElements.size());
        Consumer<T> consumer = (T value) -> recorder.add(value);

        // tryAdvance.
        assertTrue(spliterator.tryAdvance(consumer));
        assertTrue(expectedElements.contains(recorder.get(0)));

        // forEachRemaining.
        spliterator.forEachRemaining(consumer);
        Collections.sort(expectedElements, comparator);
        Collections.sort(recorder, comparator);
        assertEquals(expectedElements, recorder);

        // There should be no more elements remaining in this spliterator.
        assertFalse(spliterator.tryAdvance(consumer));
        spliterator.forEachRemaining((T) -> fail());
    }

    private static <T> void recordAndAssertBasicIteration(
            Spliterator<T> spliterator, ArrayList<T> recorder) {
        spliterator.tryAdvance(value -> recorder.add(value));
        spliterator.forEachRemaining(value -> recorder.add(value));

        // There shouldn't be any elements left in the spliterator.
        assertFalse(spliterator.tryAdvance(value -> recorder.add(value)));
        spliterator.tryAdvance(value -> fail());

        // And all subsequent splits should fail.
        assertNull(spliterator.trySplit());
    }

    public static void testSpliteratorNPE(Spliterator<?> spliterator) {
        try {
            spliterator.tryAdvance(null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            spliterator.forEachRemaining(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public static <T extends Comparable<T>> void runBasicSplitTests(
            Iterable<T> spliterable, ArrayList<T> expectedElements) {
        runBasicSplitTests(spliterable, expectedElements, T::compareTo);
    }

    public static <T> void runBasicSplitTests(Iterable<T> spliterable,
                                              ArrayList<T> expectedElements,
                                              Comparator<T> comparator) {
        ArrayList<T> recorder = new ArrayList<>();
        Spliterator<T> spliterator = spliterable.spliterator();

        // Advance the original spliterator by one element.
        assertTrue(spliterator.tryAdvance(value -> recorder.add(value)));

        // Try splitting it. Make sure that the resulting split is not null and
        // has the same characteristics as the original.
        Spliterator<T> split1 = spliterator.trySplit();
        assertNotNull(split1);
        assertEquals(spliterator.characteristics(), split1.characteristics());

        // Assert that the split iterator can itself be split.
        Spliterator<T> split1_1 = split1.trySplit();
        Spliterator<T> split1_2 = split1.trySplit();
        assertNotNull(split1_1);
        assertNotNull(split1_2);
        assertEquals(spliterator.characteristics(), split1_1.characteristics());
        assertEquals(spliterator.characteristics(), split1_2.characteristics());
        recordAndAssertBasicIteration(split1_1, recorder);
        recordAndAssertBasicIteration(split1_2, recorder);


        // Iterate over the remainder of split1.
        recordAndAssertBasicIteration(split1, recorder);

        // Advance again and make a second split.
        assertTrue(spliterator.tryAdvance(value -> recorder.add(value)));
        Spliterator<T> split2 = spliterator.trySplit();
        assertNotNull(split2);
        assertEquals(spliterator.characteristics(), split2.characteristics());
        recordAndAssertBasicIteration(split2, recorder);

        // Record all remaining elements of the original spliterator.
        recordAndAssertBasicIteration(spliterator, recorder);

        Collections.sort(expectedElements, comparator);
        Collections.sort(recorder, comparator);
        assertEquals(expectedElements, recorder);
    }

    public static <T> void runOrderedTests(Iterable<T> spliterable) {
        ArrayList<T> iteration1 = new ArrayList<>();
        ArrayList<T> iteration2 = new ArrayList<>();

        spliterable.spliterator().forEachRemaining(value -> iteration1.add(value));
        spliterable.spliterator().forEachRemaining(value -> iteration2.add(value));

        assertEquals(iteration1, iteration2);

        iteration1.clear();
        iteration2.clear();

        spliterable.spliterator().trySplit().forEachRemaining(value -> iteration1.add(value));
        spliterable.spliterator().trySplit().forEachRemaining(value -> iteration2.add(value));
        assertEquals(iteration1, iteration2);
    }

    public static <T> void runSizedTests(Iterable<T> spliterable, int expectedSize) {
        assertEquals(expectedSize, spliterable.spliterator().estimateSize());
        assertEquals(expectedSize, spliterable.spliterator().getExactSizeIfKnown());
    }

    public static <T> void runSubSizedTests(Iterable<T> spliterable, int expectedSize) {
        assertEquals(expectedSize, spliterable.spliterator().estimateSize());
        assertEquals(expectedSize, spliterable.spliterator().getExactSizeIfKnown());

        Spliterator<T> sp = spliterable.spliterator();
        Spliterator<T> split1 = sp.trySplit();

        assertEquals(expectedSize, sp.estimateSize() + split1.estimateSize());
        assertEquals(expectedSize, sp.getExactSizeIfKnown() + split1.getExactSizeIfKnown());
    }

    public static <T> void runDistinctTests(Iterable<T> spliterable) {
        HashSet<T> distinct = new HashSet<>();
        ArrayList<T> allElements = new ArrayList<>();

        Spliterator<T> spliterator = spliterable.spliterator();
        Spliterator<T> split1 = spliterator.trySplit();

        // First test that iterating via the spliterator using forEachRemaining
        // yields distinct elements.
        spliterator.forEachRemaining(value -> { distinct.add(value); allElements.add(value); });
        split1.forEachRemaining(value -> { distinct.add(value); allElements.add(value); });
        assertEquals(distinct.size(), allElements.size());

        distinct.clear();
        allElements.clear();
        spliterator = spliterable.spliterator();
        split1 = spliterator.trySplit();

        // Then test whether using tryAdvance yields the same results.
        while (spliterator.tryAdvance(value -> { distinct.add(value); allElements.add(value); })) {
        }

        while (split1.tryAdvance(value -> { distinct.add(value); allElements.add(value); })) {
        }

        assertEquals(distinct.size(), allElements.size());
    }

    public static <T extends Comparable<T>> void runSortedTests(Iterable<T> spliterable) {
        Spliterator<T> spliterator = spliterable.spliterator();
        Spliterator<T> split1 = spliterator.trySplit();

        ArrayList<T> elements = new ArrayList<>();
        spliterator.forEachRemaining(value -> elements.add(value));

        ArrayList<T> sortedElements = new ArrayList<>(elements);
        Collections.sort(sortedElements);
        assertEquals(elements, sortedElements);

        elements.clear();

        split1.forEachRemaining(value -> elements.add(value));
        sortedElements = new ArrayList<>(elements);
        Collections.sort(sortedElements);
        assertEquals(elements, sortedElements);
    }
}
