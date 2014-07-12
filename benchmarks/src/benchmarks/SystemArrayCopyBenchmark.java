/*
 * Copyright (C) 2013 Google Inc.
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
import com.google.caliper.SimpleBenchmark;

public class SystemArrayCopyBenchmark extends SimpleBenchmark {
  @Param({"2", "4", "8", "16", "32", "64", "128", "256", "512", "1024",
          "2048", "4096", "8192", "16384", "32768", "65536", "131072", "262144"})
  int arrayLength;

  // Provides benchmarking for different types of arrays using the arraycopy function.

  private int len;
  private char[] srcChar;
  private char[] dstChar;
  private byte[] srcByte;
  private byte[] dstByte;
  private short[] srcShort;
  private short[] dstShort;
  private int[] srcInt;
  private int[] dstInt;
  private long[] srcLong;
  private long[] dstLong;
  private float[] srcFloat;
  private float[] dstFloat;
  private double[] srcDouble;
  private double[] dstDouble;
  private boolean[] srcBoolean;
  private boolean[] dstBoolean;


  public void setUp() {
    len = arrayLength;
    srcChar = new char[len];
    dstChar = new char[len];
    srcByte = new byte[len];
    dstByte = new byte[len];
    srcShort = new short[len];
    dstShort = new short[len];
    srcInt = new int[len];
    dstInt = new int[len];
    srcLong = new long[len];
    dstLong = new long[len];
    srcFloat = new float[len];
    dstFloat = new float[len];
    srcDouble = new double[len];
    dstDouble = new double[len];
    srcBoolean = new boolean[len];
    dstBoolean = new boolean[len];
  }

  public void timeSystemCharArrayCopy(int reps) {
    for (int rep = 0; rep < reps; ++rep) {
      System.arraycopy(srcChar, 0, dstChar, 0, len);
    }
  }

  public void timeSystemByteArrayCopy(int reps) {
    for (int rep = 0; rep < reps; ++rep) {
      System.arraycopy(srcByte, 0, dstByte, 0, len);
    }
  }

  public void timeSystemShortArrayCopy(int reps) {
    for (int rep = 0; rep < reps; ++rep) {
      System.arraycopy(srcShort, 0, dstShort, 0, len);
    }
  }

  public void timeSystemIntArrayCopy(int reps) {
    for (int rep = 0; rep < reps; ++rep) {
      System.arraycopy(srcInt, 0, dstInt, 0, len);
    }
  }

  public void timeSystemLongArrayCopy(int reps) {
    for (int rep = 0; rep < reps; ++rep) {
      System.arraycopy(srcLong, 0, dstLong, 0, len);
    }
  }

  public void timeSystemFloatArrayCopy(int reps) {
    for (int rep = 0; rep < reps; ++rep) {
      System.arraycopy(srcFloat, 0, dstFloat, 0, len);
    }
  }

  public void timeSystemDoubleArrayCopy(int reps) {
    for (int rep = 0; rep < reps; ++rep) {
      System.arraycopy(srcDouble, 0, dstDouble, 0, len);
    }
  }

  public void timeSystemBooleanArrayCopy(int reps) {
    for (int rep = 0; rep < reps; ++rep) {
      System.arraycopy(srcBoolean, 0, dstBoolean, 0, len);
    }
  }
}
