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

import java.util.LongSummaryStatistics;

public class LongSummaryStatisticsTest extends junit.framework.TestCase {

    private long data1[] = {2, -5, 7, -1, 1, 0, 100};
    private long data2[] = {1, 3, 2, 1, 7};
    private int data3[] = {2, -5, 7, -1, 1, 0, 100};

    public void test_constructor() {
        LongSummaryStatistics lss = new LongSummaryStatistics();
        assertEquals(0, lss.getCount());
        assertEquals(0, lss.getSum());
        assertEquals(0.0d, lss.getAverage());
        assertEquals(Long.MAX_VALUE, lss.getMin());
        assertEquals(Long.MIN_VALUE, lss.getMax());
    }

    public void test_accept() {
        LongSummaryStatistics lss = new LongSummaryStatistics();
        int count = 0;
        long sum = 0;

        // For long values
        for (long value : data1) {
            lss.accept(value);
            count++;
            assertEquals(count, lss.getCount());
            sum += value;
            assertEquals(sum, lss.getSum());
        }

        // for int values
        for (int value : data3) {
            lss.accept(value);
            count++;
            assertEquals(count, lss.getCount());
            sum += value;
            assertEquals(sum, lss.getSum());
        }
    }

    public void test_combine() {
        LongSummaryStatistics lss1 = getLongSummaryStatistics1();
        LongSummaryStatistics lss2 = getLongSummaryStatistics2();
        LongSummaryStatistics lssCombine = getLongSummaryStatistics1();
        lssCombine.combine(lss2);

        assertEquals(lss1.getCount() + lss2.getCount(), lssCombine.getCount());
        assertEquals(lss1.getSum() + lss2.getSum(), lssCombine.getSum());
        assertEquals(lss1.getMax() > lss2.getMax() ? lss1.getMax() : lss2.getMax(),
                lssCombine.getMax());
        assertEquals(lss1.getMin() < lss2.getMin() ? lss1.getMin() : lss2.getMin(),
                lssCombine.getMin());
        assertEquals(((lss1.getSum() + lss2.getSum()) * 1.0)/ (lss1.getCount() + lss2.getCount()),
                lssCombine.getAverage());
    }

    public void test_getCount() {
        LongSummaryStatistics lss1 = getLongSummaryStatistics1();
        assertEquals(data1.length, lss1.getCount());
    }

    public void test_getSum() {
        LongSummaryStatistics lss1 = getLongSummaryStatistics1();
        long sum = 0;
        for (long value : data1) {
            sum += value;
        }
        assertEquals(sum, lss1.getSum());
    }

    public void test_getMin() {
        LongSummaryStatistics lss1 = getLongSummaryStatistics1();
        long min = data1[0];
        for (long value : data1) {
            min = min > value ? value : min;
        }
        assertEquals(min, lss1.getMin());
    }

    public void test_getMax() {
        LongSummaryStatistics lss1 = getLongSummaryStatistics1();
        long max = data1[0];
        for (long value : data1) {
            max = max < value ? value : max;
        }
        assertEquals(max, lss1.getMax());
    }

    public void test_getAverage() {
        LongSummaryStatistics lss1 = getLongSummaryStatistics1();
        assertEquals(lss1.getSum() * 1.0 / lss1.getCount(), lss1.getAverage());
    }

    public void test_toString() {
        LongSummaryStatistics lss1 = getLongSummaryStatistics1();
        assertEquals(String.format(
                "%s{count=%d, sum=%d, min=%d, average=%f, max=%d}",
                lss1.getClass().getSimpleName(),
                lss1.getCount(),
                lss1.getSum(),
                lss1.getMin(),
                lss1.getAverage(),
                lss1.getMax()),
                lss1.toString());
    }

    public LongSummaryStatistics getLongSummaryStatistics1() {
        LongSummaryStatistics lss = new LongSummaryStatistics();
        for (long value : data1) {
            lss.accept(value);
        }
        return lss;
    }

    public LongSummaryStatistics getLongSummaryStatistics2() {
        LongSummaryStatistics lss = new LongSummaryStatistics();
        for (long value : data2) {
            lss.accept(value);
        }
        return lss;
    }
}
