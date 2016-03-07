/*
 * Copyright (C) 2010 The Android Open Source Project
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

import java.util.IntSummaryStatistics;

public class IntSummaryStatisticsTest extends junit.framework.TestCase {

    private int data1[] = {2, -5, 7, -1, 1, 0, 100};
    private int data2[] = {1, 3, 2, 1, 7};

    public void test_constructor() {
        IntSummaryStatistics iss = new IntSummaryStatistics();
        assertEquals(0, iss.getCount());
        assertEquals(0, iss.getSum());
        assertEquals(0.0d, iss.getAverage());
        assertEquals(Integer.MAX_VALUE, iss.getMin());
        assertEquals(Integer.MIN_VALUE, iss.getMax());
    }

    public void test_accept() {
        IntSummaryStatistics iss = new IntSummaryStatistics();
        int count = 0;
        int sum = 0;
        for (int value : data1) {
            iss.accept(value);
            count++;
            assertEquals(count, iss.getCount());
            sum += value;
            assertEquals(sum, iss.getSum());
        }
    }

    public void test_combine() {
        IntSummaryStatistics iss1 = getIntSummaryStatistics1();
        IntSummaryStatistics iss2 = getIntSummaryStatistics2();
        IntSummaryStatistics issCombine = getIntSummaryStatistics1();
        issCombine.combine(iss2);

        assertEquals(iss1.getCount() + iss2.getCount(), issCombine.getCount());
        assertEquals(iss1.getSum() + iss2.getSum(), issCombine.getSum());
        assertEquals(iss1.getMax() > iss2.getMax() ? iss1.getMax() : iss2.getMax(),
                issCombine.getMax());
        assertEquals(iss1.getMin() < iss2.getMin() ? iss1.getMin() : iss2.getMin(),
                issCombine.getMin());
        assertEquals((iss1.getSum() + iss2.getSum())*1.0d / (iss1.getCount() + iss2.getCount()),
                issCombine.getAverage());
    }

    public void test_getCount() {
        IntSummaryStatistics iss1 = getIntSummaryStatistics1();
        assertEquals(data1.length, iss1.getCount());
    }

    public void test_getSum() {
        IntSummaryStatistics iss1 = getIntSummaryStatistics1();
        int sum = 0;
        for (int value : data1) {
            sum += value;
        }
        assertEquals(sum, iss1.getSum());
    }

    public void test_getMin() {
        IntSummaryStatistics iss1 = getIntSummaryStatistics1();
        int min = data1[0];
        for (int value : data1) {
            min = min > value ? value : min;
        }
        assertEquals(min, iss1.getMin());
    }

    public void test_getMax() {
        IntSummaryStatistics iss1 = getIntSummaryStatistics1();
        int max = data1[0];
        for (int value : data1) {
            max = max < value ? value : max;
        }
        assertEquals(max, iss1.getMax());
    }

    public void test_getAverage() {
        IntSummaryStatistics iss1 = getIntSummaryStatistics1();
        assertEquals(iss1.getSum() * 1.0d / iss1.getCount(), iss1.getAverage());
    }

    public void test_toString() {
        IntSummaryStatistics iss1 = getIntSummaryStatistics1();
        assertEquals(String.format(
                "%s{count=%d, sum=%d, min=%d, average=%f, max=%d}",
                iss1.getClass().getSimpleName(),
                iss1.getCount(),
                iss1.getSum(),
                iss1.getMin(),
                iss1.getAverage(),
                iss1.getMax()),
                iss1.toString());
    }

    public IntSummaryStatistics getIntSummaryStatistics1() {
        IntSummaryStatistics iss = new IntSummaryStatistics();
        for (int value : data1) {
            iss.accept(value);
        }
        return iss;
    }

    public IntSummaryStatistics getIntSummaryStatistics2() {
        IntSummaryStatistics iss = new IntSummaryStatistics();
        for (int value : data2) {
            iss.accept(value);
        }
        return iss;
    }
}
