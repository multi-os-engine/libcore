/*
 * Copyright (C) 2014 Google Inc.
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

package benchmarks.regression;

import com.google.caliper.Param;
import com.google.caliper.SimpleBenchmark;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OpenAddressingHashMap;
import java.util.Random;

public class MapBenchmark extends SimpleBenchmark {

    interface MapImpl {
        Map<HashableType, Object> getImpl(int capacity);
    }

    enum Maps implements MapImpl {
        PROBING,
        OLD;

        public Map<HashableType, Object> getImpl(int capacity) {
            if (this == PROBING) {
                return new OpenAddressingHashMap<HashableType, Object>(capacity);
            } else {
                return new HashMap<HashableType, Object>(capacity);
            }
        }
    }


    @Param({"PROBING", "OLD" }) private Maps impl;
    @Param({/* "4", */ "64", "256" /* "1024" */ }) private int size;
    @Param({"0.05", /* "0.10", "0.20", */ "0.75" }) private float collisions;
    @Param({"0.75", "1.0"}) private float duplicates;

    private static final int MIN_HASHCODE = 1000;
    private static final int MAX_HASHCODE = 2000;

    public void timeGetTypes(int n) {
        List<HashableType> entries = new HashableTypeFactory(MIN_HASHCODE, MAX_HASHCODE,
                collisions, duplicates).create(size);

        Map<HashableType, Object> map = impl.getImpl(96);
        for (HashableType t : entries) {
            map.put(t, new Object());
        }

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < entries.size(); ++j) {
                map.get(entries.get(j));
            }
        }
    }

    public void timePutTypes(int n) {
        List<HashableType> entries = new HashableTypeFactory(MIN_HASHCODE, MAX_HASHCODE,
                collisions, duplicates).create(size);

        for (int i = 0; i < n; ++i) {
            Map<HashableType, Object> map = impl.getImpl(96);
            for (HashableType t : entries) {
                map.put(t, new Object());
            }
        }
    }


    static final class HashableType implements Cloneable {
        private final int hashCode;
        private final int token;

        public HashableType(int hashCode, int token) {
            this.hashCode = hashCode;
            this.token = token;
        }

        public int getToken() {
            return token;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof HashableType)) {
                return false;
            }

            final HashableType other = (HashableType) o;
            return hashCode == other.hashCode && token == other.token;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    static final class HashableTypeFactory {
        private final int minHashCode;
        private final int maxHashCode;
        private final float collisionPercentage;
        private final float duplicatePercentage;

        public HashableTypeFactory(int minHashCode, int maxHashCode, float collisionPercentage,
                float duplicatePercentage) {
            this.minHashCode = minHashCode;
            this.maxHashCode = maxHashCode;
            this.collisionPercentage = collisionPercentage;
            this.duplicatePercentage = duplicatePercentage;
        }

        public List<HashableType> create(int size) {
            final List<HashableType> outputList = new ArrayList<HashableType>(size);
            final int numCollisions = (int) (collisionPercentage * size);
            final int numDuplicates = (int) (duplicatePercentage * numCollisions);

            if (numDuplicates > numCollisions) {
                throw new AssertionError();
            }

            final int numDistinct = size - numCollisions;
            if (numDistinct > (maxHashCode - minHashCode + 1)) {
                throw new AssertionError();
            }

            final Random random = new Random(10);

            int initialRun = size - numCollisions;
            if (initialRun == 0) {
                initialRun = 1;
            }

            for (int i = 0; i < initialRun; ++i) {
                outputList.add(new HashableType(i, i));
            }

            for (int i = 0; i < (numCollisions - numDuplicates); ++i) {
                final HashableType t = outputList.get(random.nextInt(initialRun));
                outputList.add(new HashableType(t.hashCode(), outputList.size() + i));
            }

            for (int i = 0; i < numDuplicates; ++i) {
                final HashableType t = outputList.get(random.nextInt(initialRun));
                outputList.add(new HashableType(t.hashCode(), t.getToken()));
            }

            return outputList;
        }
    }
}
