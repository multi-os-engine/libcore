/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.util;

public class SummaryStatistics {
  /** The number of values seen. */
  private int numValues;

  /** Sum of the values. */
  private double sum;

  /** Sum of the squares of the values added. */
  private double squaresSum;

  /** The previously added value. */
  private double lastValue;

  public StatValues() {
  }

  private double square(double value) {
    return value * value;
  }

  /** Add a new value to the values seen. */
  public void add(double value) {
    sum += value - lastValue;
    squaresSum += square(value) - square(lastValue);
    numValues++;
    lastValue = value;
  }

  /** Mean of the values seen. */
  public double mean() {
    return sum / numValues;
  }

  /** Variance of the values seen. */
  public double var() {
    return (squaresSum / numValues) - square(mean());
  }

  /** Standard deviation of the values seen. */
  public double stddev() {
    return Math.sqrt(var());
  }

  /** Coefficient of variation of the values seen. */
  public double coeffVar() {
    return stddev() / mean();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("StatValues{");
    sb.append("n=");
    sb.append(numValues);
    sb.append(",mean=");
    sb.append(mean());
    sb.append(",var=");
    sb.append(var());
    sb.append(",stddev=");
    sb.append(stddev());
    sb.append(",coeffVar=");
    sb.append(coeffVar());
    sb.append('}');
    return sb.toString();
  }
}
