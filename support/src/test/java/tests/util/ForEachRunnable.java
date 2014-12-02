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
 * limitations under the License.
 */

package tests.util;

/**
 * Runner which executes the provided code under test for each provided input value.
 *
 * <p>Subclasses need to override {@link #run(Object) run} which is invoked for each input value.
 */
public abstract class ForEachRunnable<T> {

  private String mValueName;

  /**
   * Invoked for each input value.
   */
  protected abstract void run(T value) throws Exception;

  /**
   * Gets the name associated with the current value.
   *
   * @return name or {@code null} if no name is available.
   */
  protected String getValueName() {
    return mValueName;
  }

  /**
   * Invokes {@link #run(Object) run} for each provided value.
   */
  public void runAll(Iterable<T> values) throws Exception {
    for (T value : values) {
      try {
        run(value);
      } catch (Throwable e) {
        throw new Exception("Failed for " + value, e);
      }
    }
  }

  /**
   * Invokes {@link #run(Object) run} for each provided value. In case of a failure, the name of the
   * value for which the failure occurred will be reported in addition to the failure itself.
   */
  public void runAllNamed(Iterable<Pair<String, T>> namedValues) throws Exception {
    for (Pair<String, T> nameAndValue : namedValues) {
      String name = nameAndValue.getFirst();
      T value = nameAndValue.getSecond();
      try {
        mValueName = name;
        run(nameAndValue.getSecond());
      } catch (Throwable e) {
        throw new Exception("Failed for \"" + name + "\" " + value, e);
      }
    }
  }
}
