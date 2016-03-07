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

import java.util.DoubleSummaryStatistics;

public class DoubleSummaryStatisticsTest extends junit.framework.TestCase {

    private double data1[] = {22.4, -53.4, 74.8, -12.4, 17, 0, 100};
    private double data2[] = {1.2, 3.5, 2.7, 1, 7.6};

    public void test_constructor() {
        DoubleSummaryStatistics dss = new DoubleSummaryStatistics();
        assertEquals(0, dss.getCount());
        assertEquals(0.0d, dss.getSum());
        assertEquals(0.0d, dss.getAverage());
        assertEquals(Double.POSITIVE_INFINITY, dss.getMin());
        assertEquals(Double.NEGATIVE_INFINITY, dss.getMax());
    }

    public void test_accept() {
        DoubleSummaryStatistics dss = new DoubleSummaryStatistics();
        int count = 0;
        double sum = 0;
        for (double value : data1) {
            dss.accept(value);
            count++;
            assertEquals(count, dss.getCount());
            sum += value;
            assertEquals((int)(sum*100000), (int)(100000*dss.getSum()));
        }
    }

    public void test_combine() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatistics1();
        DoubleSummaryStatistics dss2 = getDoubleSummaryStatistics2();
        DoubleSummaryStatistics dssCombine = getDoubleSummaryStatistics1();
        dssCombine.combine(dss2);

        assertEquals(dss1.getCount() + dss2.getCount(), dssCombine.getCount());
        assertEquals(dss1.getSum() + dss2.getSum(), dssCombine.getSum());
        assertEquals(dss1.getMax() > dss2.getMax() ? dss1.getMax() : dss2.getMax(),
                dssCombine.getMax());
        assertEquals(dss1.getMin() < dss2.getMin() ? dss1.getMin() : dss2.getMin(),
                dssCombine.getMin());
        assertEquals((dss1.getSum() + dss2.getSum()) / (dss1.getCount() + dss2.getCount()),
                dssCombine.getAverage());
    }

    public void test_getCount() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatistics1();
        assertEquals(data1.length, dss1.getCount());
    }

    public void test_getSum() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatistics1();
        double sum = 0;
        for (double value : data1) {
            sum += value;
        }
        assertEquals(sum, dss1.getSum());

        dss1.accept(Double.NaN);
        assertEquals(Double.NaN, dss1.getSum());
    }

    public void test_getMin() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatistics1();
        double min = data1[0];
        for (double value : data1) {
            min = min > value ? value : min;
        }
        assertEquals(min, dss1.getMin());

        dss1.accept(Double.NaN);
        assertEquals(Double.NaN, dss1.getMin());
    }

    public void test_getMax() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatistics1();
        double max = data1[0];
        for (double value : data1) {
            max = max < value ? value : max;
        }
        assertEquals(max, dss1.getMax());

        dss1.accept(Double.NaN);
        assertEquals(Double.NaN, dss1.getMax());
    }

    public void test_getAverage() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatistics1();
        assertEquals(dss1.getSum() / dss1.getCount(), dss1.getAverage());

        dss1.accept(Double.NaN);
        assertEquals(Double.NaN, dss1.getAverage());
    }

    public void test_toString() {
        DoubleSummaryStatistics dss1 = getDoubleSummaryStatistics1();
        assertEquals(String.format(
                "%s{count=%d, sum=%f, min=%f, average=%f, max=%f}",
                dss1.getClass().getSimpleName(),
                dss1.getCount(),
                dss1.getSum(),
                dss1.getMin(),
                dss1.getAverage(),
                dss1.getMax()),
                dss1.toString());
    }

    public DoubleSummaryStatistics getDoubleSummaryStatistics1() {
        DoubleSummaryStatistics dss = new DoubleSummaryStatistics();
        for (double value : data1) {
            dss.accept(value);
        }
        return dss;
    }

    public DoubleSummaryStatistics getDoubleSummaryStatistics2() {
        DoubleSummaryStatistics dss = new DoubleSummaryStatistics();
        for (double value : data2) {
            dss.accept(value);
        }
        return dss;
    }
}
