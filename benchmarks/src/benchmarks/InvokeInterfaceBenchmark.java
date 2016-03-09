/*
 * Copyright (C) 2016 Google Inc.
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

import java.util.ArrayList;
import java.util.List;

public class InvokeInterfaceBenchmark {
    static ArrayList<Integer> list;
    static {
        list = new ArrayList<>(256);
        for (int i = 0; i < 256; ++i) {
            list.add(0);
        }
    }

    public void timeArrayListSetInterface(int nreps) {
        ArrayList<Integer> list1 = list;

        for (int i = 0; i < nreps; ++i) {
            doSet(list1);
        }
    }

    public void timeArrayListSetconcrete(int nreps) {
        ArrayList<Integer> list1 = list;

        for (int i = 0; i < nreps; ++i) {
            doSetConcrete(list1);
        }
    }

    public void doSetConcrete(ArrayList<Integer> list1) {
        list1.set(128, 128);
        list1.get(128);
    }

    public void doSet(List<Integer> list1) {
        list1.set(128, 128);
        list1.get(128);
    }
}
