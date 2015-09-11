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

import com.google.caliper.SimpleBenchmark;

import java.util.Locale;

import libcore.icu.ICU;

public class IcuBenchmark extends SimpleBenchmark {

    private static final String ASCII_LOWERCASE;
    private static final String ASCII_UPPERCASE;

    private static final String LAT_1_SUPPLEMENT;  // U+00C0 - U+00FF
    private static final String LAT_EXTENDED_A;  // U+0100 - U+017F
    private static final String LAT_EXTENDED_B;  // U+0180 - U+024F

    private static final Locale CZECH_LOCALE = Locale.forLanguageTag("cs-CZ");
    private static final Locale PINYIN_LOCALE = Locale.forLanguageTag("zh-Latn");

    static {
        char[] tmp = new char[26];

        for (int i = 97; i < 123; i++) {
            tmp[i - 97] = (char) i;
        }

        ASCII_LOWERCASE = new String(tmp);

        for (int i = 65; i < 91; i++) {
            tmp[i - 65] = (char) i;
        }

        ASCII_UPPERCASE = new String(tmp);

        // Latin 1 Supplement (character part) has char codes U+00C0 - U+00FF
        tmp = new char[0x3F];

        for (int i = 0xC0; i < 0xFF; i++) {
            tmp[i - 0xC0] = (char) i;
        }

        LAT_1_SUPPLEMENT = new String(tmp);

        // Latin Extended A has char codes U+0100 - U+017F
        tmp = new char[0x80];

        for (int i = 0x100; i < 0x180; i++) {
            tmp[i - 0x100] = (char) i;
        }

        LAT_EXTENDED_A = new String(tmp);

        // Latin Extended B has char codes U+0180 - U+024F
        tmp = new char[0xCF];

        for (int i = 0x180; i < 0x24F; i++) {
            tmp[i - 0x180] = (char) i;
        }

        LAT_EXTENDED_B = new String(tmp);
    }


    public void time_getBestDateTimePattern(int reps) throws Exception {
        for (int rep = 0; rep < reps; ++rep) {
            ICU.getBestDateTimePattern("dEEEMMM", new Locale("en", "US"));
        }
    }

    // Convert standard lowercase ASCII characters to uppercase using ICU4C in the US locale.
    public void time_toUpperCaseAsciiUS(int reps) {
        for (int i = 0; i < reps; i++) {
            ICU.toUpperCase(ASCII_LOWERCASE, Locale.US);
        }
    }

    // Convert standard uppercase ASCII characters to lowercase.
    public void time_toLowerCaseAsciiUs(int reps) {
        for (int i = 0; i < reps; i++) {
            ICU.toLowerCase(ASCII_UPPERCASE, Locale.US);
        }
    }

    // Convert Latin 1 supplement characters to uppercase in France locale.
    public void time_toUpperCaseLat1SuplFr(int reps) {
        for (int i = 0; i < reps; i++) {
            ICU.toUpperCase(LAT_1_SUPPLEMENT, Locale.FRANCE);
        }
    }

    // Convert Latin Extension A characters to uppercase in Czech locale
    public void time_toUpperCaseLatExtACz(int reps) {
        for (int i = 0; i < reps; i++) {
            ICU.toUpperCase(LAT_EXTENDED_A, CZECH_LOCALE);
        }
    }

    // Convert Latin Extension B characters to uppercase in Pinyin locale.
    public void time_toUpperCaseLatExtBPinyin(int reps) {
        for (int i = 0; i < reps; i++) {
            ICU.toUpperCase(LAT_EXTENDED_B, PINYIN_LOCALE);
        }
    }

}
