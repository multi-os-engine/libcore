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

package benchmarks;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.util.Arrays;


public class BufferedWriterBenchmark extends SimpleBenchmark {
    @Param( {"8192", "16384", "32768"})
    private int charsToWrite;
    private CharArrayWriter mArrayWriter;

    public int timeSingleCharAppends(int reps) throws Exception {
        int res = 0;
        CharArrayWriter arrayWriter = mArrayWriter;
        for (; reps > 0; reps--) {
            arrayWriter.reset();
            BufferedWriter w = new BufferedWriter(arrayWriter);
            for (int i = 0; i < charsToWrite; i++) {
                w.append('x');
            }
            w.close();
            res |= arrayWriter.size();
        }
        return res;
    }

    public int timeSingleCharWrites(int reps) throws Exception {
        int res = 0;
        CharArrayWriter arrayWriter = mArrayWriter;
        for (; reps > 0; reps--) {
            arrayWriter.reset();
            BufferedWriter w = new BufferedWriter(arrayWriter);
            for (int i = 0; i < charsToWrite; i++) {
                w.write('x');
            }
            w.close();
            res |= arrayWriter.size();
        }
        return res;
    }

    @Override protected void setUp() throws Exception {
        mArrayWriter = new CharArrayWriter(charsToWrite);
    }

    public static void main(String[] args) {
        Runner.main(BufferedWriterBenchmark.class, args);
    }
}
