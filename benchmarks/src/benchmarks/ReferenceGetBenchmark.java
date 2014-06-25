/*
 * Copyright (C) 2014 The Android Open Source Project
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

package benchmarks;

import com.google.caliper.Param;
import com.google.caliper.SimpleBenchmark;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;

public class ReferenceGetBenchmark extends SimpleBenchmark {
    // make gc request, not necessarily performed
    @Param boolean forceSlowPath;

    private Object obj = "str";

    protected void setUp() throws Exception {
        if (forceSlowPath) {
            Field slowPathMask = Reference.class.getDeclaredField("intrinsicEnabledMask");
            slowPathMask.setAccessible(true);
            slowPathMask.setInt(null, ~0x0);
        }
    }

    public void timeSoftReferenceGet(int reps) throws Exception {
        Reference soft = new SoftReference(obj);
        for (int i = 0; i < reps; i++) {
            Object o = soft.get();
        }
    }

    public void timeWeakReferenceGet(int reps) throws Exception {
        Reference weak = new WeakReference(obj);
        for (int i = 0; i < reps; i++) {
            Object o = weak.get();
        }
    }

    public void timeNonPreservedWeakReferenceGet(int reps) throws Exception {
        Reference weak = new WeakReference(obj);
        obj = null;
        Runtime.getRuntime().gc();
        for (int i = 0; i < reps; i++) {
            Object o = weak.get();
        }
    }
}
