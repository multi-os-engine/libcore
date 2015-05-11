/*
 * Copyright (C) 2015 The Android Open Source Project
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

import java.util.Arrays;
import java.util.Comparator;

/**
 * This test is based on test data generated by
 * https://github.com/abstools/java-timsort-bug/blob/master/TestTimSort.java
 */
public class TimSortTest extends TestCase {

    private static final Comparator<Integer> NATURAL_ORDER_COMPARATOR = new Comparator<Integer>() {
        public int compare(Integer first, Integer second) {
            return first.compareTo(second);
        }
    };

    private static final int BAD_DATA_SIZE = 65536;
    private static int[] BAD_RUN_OFFSETS = {
            20204, 20221, 20237, 20255, 20289, 20363, 20521, 20837, 21469, 22733, 25260, 30315,
            40408, 40425, 40441, 40459, 40493, 40567, 40725, 41041, 41673, 42936, 45463, 50500,
            50517, 50533, 50551, 50585, 50659, 50817, 51133, 51764, 53027, 55536, 55553, 55569,
            55587, 55621, 55695, 55853, 56168, 56799, 58044, 58061, 58077, 58095, 58129, 58203,
            58360, 58675, 59288, 59305, 59321, 59339, 59373, 59446, 59603, 59900, 59917, 59933,
            59951, 59985, 60059, 60196, 60217, 60236, 60274, 60332, 60351, 60369, 60389, 60405,
    };

    public void testBug19493779WithComparable() throws Exception {
        Integer[] array = createBugTriggerData();
        Arrays.sort(array);
        // The bug caused an ArrayIndexOutOfBoundsException, but we check this anyway.
        assertSorted(array);
    }

    public void testBug19493779WithComparator() throws Exception {
        Integer[] array = createBugTriggerData();
        Arrays.sort(array, NATURAL_ORDER_COMPARATOR);
        // The bug caused an ArrayIndexOutOfBoundsException, but we check this anyway.
        assertSorted(array);
    }

    private static void assertSorted(Integer[] arrayToSort) {
        for (int i = 1; i < arrayToSort.length; i++) {
            if (arrayToSort[i - 1] > arrayToSort[i]) {
                fail("Array not sorted at element " + i + ": " + Arrays.toString(arrayToSort));
            }
        }
    }

    private static Integer[] createBugTriggerData() {
        final Integer zero = 0;
        final Integer one = 1;

        Integer[] bugTriggerData = new Integer[BAD_DATA_SIZE];
        for (int i = 0; i < bugTriggerData.length; i++) {
            bugTriggerData[i] = zero;
        }

        for (int i = 0; i < BAD_RUN_OFFSETS.length; i++) {
            bugTriggerData[BAD_RUN_OFFSETS[i]] = one;
        }
        return bugTriggerData;
    }
}