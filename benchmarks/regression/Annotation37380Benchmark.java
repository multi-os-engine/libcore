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

package benchmarks.regression;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Annotation37380Benchmark extends SimpleBenchmark {

  private Field field;
  private FieldAnnotation annotation;
  {
    try {
      field = MyClass.class.getDeclaredField("field");
      annotation = field.getAnnotation(FieldAnnotation.class);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public void timeGetAnnotation(int reps) throws Exception {
    for (int i = 0; i < reps; ++i) {
      field.getAnnotation(FieldAnnotation.class);
    }
  }

  public void timeGetValue(int reps) throws Exception {
    for (int i = 0; i < reps; ++i) {
      annotation.property();
    }
  }

  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  private static @interface FieldAnnotation {
    String property();
  }

  private static class MyClass {
    @FieldAnnotation(property = "value") String field;
  }

  public static void main(String[] args) throws Exception {
    Runner.main(Annotation37380Benchmark.class, args);
  }
}
