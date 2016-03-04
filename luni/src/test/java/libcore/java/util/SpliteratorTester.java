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
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static junit.framework.Assert.*;

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

    public static <T extends Comparable<T>> void runSplitTests(Iterable<T> spliterable,
                                                               ArrayList<T> expectedElements,
                                                               boolean isOrdered) {


    }
}
