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

import junit.framework.TestCase;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public class SpliteratorsTest extends TestCase {

    public void testEmptyIntSpliterator() {
        Spliterator.OfInt empty = Spliterators.emptyIntSpliterator();
        assertNull(empty.trySplit());
        assertEquals(0, empty.estimateSize());
        assertEquals(0, empty.getExactSizeIfKnown());

        IntConsumer alwaysFails = (int value) -> fail();
        Consumer<Integer> alwaysFailsBoxed = (Integer value) -> fail();
        empty.tryAdvance(alwaysFails);
        empty.tryAdvance(alwaysFailsBoxed);

        empty.forEachRemaining(alwaysFails);
        empty.forEachRemaining(alwaysFailsBoxed);

        assertEquals(Spliterator.SIZED | Spliterator.SUBSIZED, empty.characteristics());
    }

    public void testEmptyRefSpliterator() {
        Spliterator<Object> empty = Spliterators.emptySpliterator();
        assertNull(empty.trySplit());
        assertEquals(0, empty.estimateSize());
        assertEquals(0, empty.getExactSizeIfKnown());

        Consumer<Object> alwaysFails = (Object value) -> fail();
        empty.tryAdvance(alwaysFails);
        empty.forEachRemaining(alwaysFails);

        assertEquals(Spliterator.SIZED | Spliterator.SUBSIZED, empty.characteristics());
    }

    public void testEmptyLongSpliterator() {
        Spliterator.OfLong empty = Spliterators.emptyLongSpliterator();
        assertNull(empty.trySplit());
        assertEquals(0, empty.estimateSize());
        assertEquals(0, empty.getExactSizeIfKnown());

        LongConsumer alwaysFails = (long value) -> fail();
        Consumer<Long> alwaysFailsBoxed = (Long value) -> fail();
        empty.tryAdvance(alwaysFails);
        empty.tryAdvance(alwaysFailsBoxed);

        empty.forEachRemaining(alwaysFails);
        empty.forEachRemaining(alwaysFailsBoxed);

        assertEquals(Spliterator.SIZED | Spliterator.SUBSIZED, empty.characteristics());
    }

    public void testEmptyDoubleSpliterator() {
        Spliterator.OfDouble empty = Spliterators.emptyDoubleSpliterator();
        assertNull(empty.trySplit());
        assertEquals(0, empty.estimateSize());
        assertEquals(0, empty.getExactSizeIfKnown());

        DoubleConsumer alwaysFails = (double value) -> fail();
        Consumer<Double> alwaysFailsBoxed = (Double value) -> fail();
        empty.tryAdvance(alwaysFails);
        empty.tryAdvance(alwaysFailsBoxed);

        empty.forEachRemaining(alwaysFails);
        empty.forEachRemaining(alwaysFailsBoxed);

        assertEquals(Spliterator.SIZED | Spliterator.SUBSIZED, empty.characteristics());
    }

    public void testSpliteratorObjectArray() {
        String[] array = { "a", "b", "c", "d", "e", "f", "g", "h" };
        Spliterator<String> sp = Spliterators.spliterator(array, 0);
        assertEquals(8, sp.estimateSize());
        assertEquals(8, sp.getExactSizeIfKnown());



    }
}
