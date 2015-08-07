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

package benchmarks.regression;

import com.google.caliper.SimpleBenchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


public class InputStreamReaderBenchmark extends SimpleBenchmark {

    // Repeat the reading a few times to make differences more pronounced.
    private static final int SCALING_FACTOR = 5;

    private static final int SMALL_LINES = 2 << 5;
    private static final int MEDIUM_LINES = 2 << 10;
    private static final int LARGE_LINES = 2 << 12;
    private static final int VLARGE_LINES = 2 << 14;

    private static final String LINE = "This is a sample line which is used for testing.\n";

    private static final String BASE = "/storage/sdcard0/";

    private static void writeFile(String fileName, int lines) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        for (int i = 0; i < lines; i++) {
            writer.write(LINE);
        }
        writer.close();
    }

    static {
        try {
            File small = new File(BASE + "small");
            File medium = new File(BASE + "medium");
            File large = new File(BASE + "large");
            File vlarge = new File(BASE + "vlarge");

            if (!small.exists()) {
                writeFile(BASE + "small", SMALL_LINES);
            }
            if (!medium.exists()) {
                writeFile(BASE + "medium", MEDIUM_LINES);
            }
            if (!large.exists()) {
                writeFile(BASE + "large", LARGE_LINES);
            }
            if (!vlarge.exists()) {
                writeFile(BASE + "vlarge", VLARGE_LINES);
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }


    public void time_small(int reps) throws IOException {
        readerDelegate(reps, BASE + "small");
    }

    public void time_medium(int reps) throws IOException {
        readerDelegate(reps, BASE + "medium");
    }

    public void time_large(int reps) throws IOException {
        readerDelegate(reps, BASE + "large");
    }

    public void time_vlarge(int reps) throws IOException {
        readerDelegate(reps, BASE + "vlarge");
    }

    public void readerDelegate(int reps, String fileName) throws IOException {
        for (int i = 0; i < reps * SCALING_FACTOR; i++) {
            InputStreamReader is = new InputStreamReader(
                    new FileInputStream(fileName));
            BufferedReader br = new BufferedReader(is);

            while (br.readLine() != null) {
                    /* Line has been read */
            }

            br.close();
        }
    }
}
