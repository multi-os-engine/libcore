/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package benchmarks;

import com.google.caliper.Param;
import com.google.caliper.SimpleBenchmark;
import java.util.Arrays;

public class DeepArrayOpsBenchmark extends SimpleBenchmark {
    @Param({"1", "4", "16", "256", "2048"}) int arrayLength;

    private Object[] array;
    private Object[] array2;

    protected void setUp() throws Exception {
        array = new Object[arrayLength * 10];
        array2 = new Object[arrayLength * 10];
        for (int i = 0; i < arrayLength; i += 10) {
            array[i] = new IntWrapper(i);
            array2[i] = new IntWrapper(i);

            array[i + 1] = new16ElementObjectarray();
            array2[i + 1] = new16ElementObjectarray();

            array[i + 2] = new boolean[16];
            array2[i + 2] = new boolean[16];

            array[i + 3] = new byte[16];
            array2[i + 3] = new byte[16];

            array[i + 4] = new char[16];
            array2[i + 4] = new char[16];

            array[i + 5] = new short[16];
            array2[i + 5] = new short[16];

            array[i + 6] = new float[16];
            array2[i + 6] = new float[16];

            array[i + 7] = new long[16];
            array2[i + 7] = new long[16];

            array[i + 8] = new int[16];
            array2[i + 8] = new int[16];

            array[i + 9] = new double[16];
            array2[i + 9] = new double[16];
        }
    }

    public void timeDeepHashCode(int reps) {
        for (int i = 0; i < reps; ++i) {
            Arrays.deepHashCode(array);
        }
    }

    public void timeEquals(int reps) {
        for (int i = 0; i < reps; ++i) {
            Arrays.deepEquals(array, array2);
        }
    }

    private static final Object[] new16ElementObjectarray() {
        Object[] array = new Object[16];
        for (int i = 0; i < 16; ++i) {
            array[i] = new IntWrapper(i);
        }

        return array;
    }

    public static final class IntWrapper {
        private final int wrapped;

        public IntWrapper(int wrap) {
            wrapped = wrap;
        }

        @Override
        public int hashCode() {
            return wrapped;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IntWrapper)) {
                return false;
            }

            return ((IntWrapper) o).wrapped == this.wrapped;
        }
    }
}

