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

import java.util.Locale;

public class LocaleBenchmark extends SimpleBenchmark {

    private static final Locale withLanguageExtension = new Locale("de", "DE",
            "co-phonebk-kc-kv-space");

    private Locale[] set = new Locale[] {
            Locale.US,
            Locale.FRANCE,
            Locale.GERMANY,
            Locale.CHINA,
            Locale.FRANCE,
            Locale.KOREA,
            Locale.JAPAN,
            withLanguageExtension
    };

    public void time_toLanguageTag(int reps) {
        for (int i = 0; i < reps; i++) {
            for (Locale inSet : set) {
                inSet.toLanguageTag();
            }
        }
    }

    // Covers getDisplay{language, country, variant, script}.
    public void time_getDisplayName(int reps) {
        for (int i = 0; i < reps; i++) {
            for (Locale inSet : set) {
                inSet.getDisplayName();
            }
        }
    }

}
