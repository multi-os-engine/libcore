/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package benchmarks.regression;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import junit.framework.Assert;

/**
 * Benchmarks to measure the performance of String.equals for Strings of varying lengths.
 * Each benchmarks makes 5 measurements, aiming at covering cases like strings of equal length
 * that are not equal, identical strings with different references, strings with different endings,
 * interned strings, and strings of different lengths.
 */
public class StringEqualsBenchmark extends SimpleBenchmark {
    private final String long1 = "Ahead-of-time compilation is possible as the compiler may just"
        + "convert an instruction thus: dex code: add-int v1000, v2000, v3000 C code: setIntRegter"
        + "(1000, call_dex_add_int(getIntRegister(2000), getIntRegister(3000)) This means even lid"
        + "instructions may have code generated, however, it is not expected that code generate in"
        + "this way will perform well. The job of AOT verification is to tell the compiler that"
        + "instructions are sound and provide tests to detect unsound sequences so slow path code"
        + "may be generated. Other than for totally invalid code, the verification may fail at AOr"
        + "run-time. At AOT time it can be because of incomplete information, at run-time it can e"
        + "that code in a different apk that the application depends upon has changed. The Dalvik"
        + "verifier would return a bool to state whether a Class were good or bad. In ART the fail"
        + "case becomes either a soft or hard failure. Classes have new states to represent that a"
        + "soft failure occurred at compile time and should be re-verified at run-time.";

    private final String veryLong = "Garbage collection has two phases. The first distinguishes"
        + "live objects from garbage objects.  The second is reclaiming the rage of garbage object"
        + "In the mark-sweep algorithm used by Dalvik, the first phase is achievd by computing the"
        + "closure of all reachable objects in a process known as tracing from theoots.  After the"
        + "trace has completed, garbage objects are reclaimed.  Each of these operations can be"
        + "parallelized and can be interleaved with the operation of the applicationTraditionally,"
        + "the tracing phase dominates the time spent in garbage collection.  The greatreduction i"
        + "pause time can be achieved by interleaving as much of this phase as possible with the"
        + "application. If we simply ran the GC in a separate thread with no other changes, normal"
        + "operation of an application would confound the trace.  Abstractly, the GC walks the h o"
        + "all reachable objects.  When the application is paused, the object graph cannot change."
        + "The GC can therefore walk this structure and assume that all reachable objects live."
        + "When the application is running, this graph may be altered. New nodes may be addnd edge"
        + "may be changed.  These changes may cause live objects to be hidden and falsely recla by"
        + "the GC.  To avoid this problem a write barrier is used to intercept and record modifion"
        + "to objects in a separate structure.  After performing its walk, the GC will revisit the"
        + "updated objects and re-validate its assumptions.  Without a card table, the garbage"
        + "collector would have to visit all objects reached during the trace looking for dirtied"
        + "objects.  The cost of this operation would be proportional to the amount of live data."
        + "With a card table, the cost of this operation is proportional to the amount of updateat"
        + "The write barrier in Dalvik is a card marking write barrier.  Card marking is the proce"
        + "of noting the location of object connectivity changes on a sub-page granularity.  A car"
        + "is merely a colorful term for a contiguous extent of memory smaller than a page, common"
        + "somewhere between 128- and 512-bytes.  Card marking is implemented by instrumenting all"
        + "locations in the virtual machine which can assign a pointer to an object.  After themal"
        + "pointer assignment has occurred, a byte is written to a byte-map spanning the heap whic"
        + "corresponds to the location of the updated object.  This byte map is known as a card ta"
        + "The garbage collector visits this card table and looks for written bytes to reckon the"
        + "location of updated objects.  It then rescans all objects located on the dirty card,"
        + "correcting liveness assumptions that were invalidated by the application.  While card"
        + "marking imposes a small burden on the application outside of a garbage collection, the"
        + "overhead of maintaining the card table is paid for by the reduced time spent inside"
        + "garbage collection. With the concurrent garbage collection thread and a write barrier"
        + "supported by the interpreter, JIT, and Runtime we modify garbage collection";

    private final String[] shortStrings1 = new String[] {"a", ":", "ja M", "$$$", "hi"};

    private final String[] shortStrings2 = new String[] {"a", " :", "ja N", "$$", "hi"};

    private final String[] mediumStrings1 = new String[] {
        "Hello my name is ", // constant
        "What's your name?",
        "Android Runtime",
        "v3ry Cre@tiVe?****",
        "!@#$%^&*()_++*^$#@"
    };

    private final String[] mediumStrings2 = new String[] {
        "Hello my name is ",
        "Whats your name?",
        "Android Runtime",
        "v3ry Cre@tiVe?***",
        "0@#$%^&*()_++*^$#@"
    };

    private final String[] longStrings1 = new String[] {
        long1,
        long1 + "fun!",
        long1 + long1,
        long1 + "123456789",
        "Android Runtime" + long1
    };

    private final String[] longStrings2 = new String[] {
        new String(long1), // force execution of code beyond reference equality check
        "----" + long1,
        long1 + long1,
        long1 + "12345678",
        "android Runtime" + long1
    };

    private final String[] veryLongStrings1 = new String[] {
        veryLong,
        veryLong + veryLong,
        veryLong + veryLong + veryLong,
        veryLong + "77777",
        "Android Runtime" + veryLong
    };

    private final String[] veryLongStrings2 = new String[] {
        new String(veryLong), // force execution of code beyond reference equality check
        veryLong + " " + veryLong,
        veryLong + veryLong + veryLong,
        veryLong + "99999",
        "android" + veryLong
    };

    private final String[] endStrings1 = new String[] {
        "Hello",
        long1,
        veryLong,
        "How are you doing today?",
        "1"
    };

    private final String[] endStrings2 = new String[] {
        "Hello ",
        long1 + "x",
        veryLong + "?",
        "How are you doing today ",
        "1."
    };

    private final String tmpStr1 = "012345678901234567890"
        + "0123456789012345678901234567890123456789"
        + "0123456789012345678901234567890123456789"
        + "0123456789012345678901234567890123456789"
        + "0123456789012345678901234567890123456789";

    private final String tmpStr2 = "z012345678901234567890"
        + "0123456789012345678901234567890123456789"
        + "0123456789012345678901234567890123456789"
        + "0123456789012345678901234567890123456789"
        + "012345678901234567890123456789012345678x";

    private final String[] nonalignedStrings1 = new String[] {
        tmpStr1,
        tmpStr2,
        long1,
        veryLong,
        "hello"
    };

    private final String[] nonalignedStrings2 = new String[] {
        tmpStr1.substring(1), // string is no longer word aligned
        tmpStr2.substring(2),
        long1.substring(3),
        veryLong.substring(1),
        "hello".substring(0)
    };

    private final String[] internedStrings1 = new String[] {
        tmpStr1.intern(), // interned string
        tmpStr2.intern(),
        long1.intern(),
        veryLong.intern(),
        "hi".intern()
    };

    private final String[] internedStrings2 = new String[] {
        new String(tmpStr1).intern(),
        new String(tmpStr2).intern(),
        new String(long1).intern(),
        new String(veryLong).intern(),
        "hello".intern()
    };

    private final Object[] objects = new Object[] {
        new Double(1.5),
        new Integer(9999999),
        new String[] {"h", "i"},
        new int[] {1, 2, 3},
        new Character('a')
    };

    // Check that all objects expected to be references to the same object actually are
    @Override protected void setUp() throws Exception {
        Assert.assertSame("abc", "abc"); // check string constants are the same object
        Assert.assertSame(tmpStr1.intern(), new String(tmpStr1).intern());
        Assert.assertSame(long1 + long1, long1 + long1);
    }

    // Benchmark cases of String.equals(null)
    public void timeEqualsNull(int reps) {
        for (int rep = 0; rep < reps; ++rep) {
            for (int i = 0; i < mediumStrings1.length; i++) {
                mediumStrings1[i].equals(null);
            }
        }
    }

    // Benchmark cases with very short (<5 character) Strings
    public void timeEqualsShort(int reps) {
        for (int rep = 0; rep < reps; ++rep) {
            for (int i = 0; i < shortStrings1.length; i++) {
                shortStrings1[i].equals(shortStrings2[i]);
            }
        }
    }

    // Benchmark cases with medium length (10-15 character) Strings
    public void timeEqualsMedium(int reps) {
        for (int rep = 0; rep < reps; ++rep) {
            for (int i = 0; i < mediumStrings1.length; i++) {
                mediumStrings1[i].equals(mediumStrings2[i]);
            }
        }
    }

    // Benchmark cases with long (>100 character) Strings
    public void timeEqualsLong(int reps) {
        for (int rep = 0; rep < reps; ++rep) {
            for (int i = 0; i < longStrings1.length; i++) {
                longStrings1[i].equals(longStrings2[i]);
            }
        }
    }

    // Benchmark cases with very long (>1000 character) Strings
    public void timeEqualsVeryLong(int reps) {
        for (int rep = 0; rep < reps; ++rep) {
            for (int i = 0; i < veryLongStrings1.length; i++) {
                veryLongStrings1[i].equals(veryLongStrings2[i]);
            }
        }
    }

    // Benchmark cases with non-word aligned Strings
    public void timeEqualsNonWordAligned(int reps) {
        for (int rep = 0; rep < reps; ++rep) {
            for (int i = 0; i < nonalignedStrings1.length; i++) {
                nonalignedStrings1[i].equals(nonalignedStrings2[i]);
            }
        }
    }

    // Benchmark cases with slight differences in the endings
    public void timeEqualsEnd(int reps) {
        for (int rep = 0; rep < reps; ++rep) {
            for (int i = 0; i < endStrings1.length; i++) {
                endStrings1[i].equals(endStrings2[i]);
            }
        }
    }

    // Benchmark cases with interned strings
    public void timeInterned(int reps) {
        for (int rep = 0; rep < reps; ++rep) {
            for (int i = 0; i < internedStrings1.length; i++) {
                internedStrings1[i].equals(internedStrings2[i]);
            }
        }
    }

    // Benchmark cases of comparing a string to a non-string object
    public void timeEqualsNonString(int reps) {
        for (int rep = 0; rep < reps; ++rep) {
            for (int i = 0; i < mediumStrings1.length; i++) {
                mediumStrings1[i].equals(objects[i]);
            }
        }
    }
}
