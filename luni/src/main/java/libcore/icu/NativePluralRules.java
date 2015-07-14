/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.icu;

import com.ibm.icu.text.PluralRules;

import java.util.Locale;

/**
 * Provides access to ICU's
 * <a href="http://icu-project.org/apiref/icu4j/com/ibm/icu/text/PluralRules.html">PluralRules</a> class.
 * This is not necessary for Java API, but is used by frameworks/base's resources system to
 * ease localization of strings to languages with complex grammatical rules regarding number.
 */
public final class NativePluralRules {
    public static final int ZERO  = 0;
    public static final int ONE   = 1;
    public static final int TWO   = 2;
    public static final int FEW   = 3;
    public static final int MANY  = 4;
    public static final int OTHER = 5;

    private NativePluralRules(PluralRules jInstance) {
        this.jInstance = jInstance;
    }

    public static NativePluralRules forLocale(Locale locale) {
        return new NativePluralRules(PluralRules.forLocale(locale));
    }

    private final PluralRules jInstance;

    /**
     * Returns the constant defined in this class corresponding
     * to the first rule that matches the given value.
     */
    public int quantityForInt(int value) {
        // Pre-L compatibility. http://b/18429565.
        if (value < 0) {
            return OTHER;
        }
        String keyword = jInstance.select(value);
        if (keyword.equals("zero")) {
            return 0;
        } else if (keyword.equals("one")) {
            return 1;
        } else if (keyword.equals("two")) {
            return 2;
        } else if (keyword.equals("few")) {
            return 3;
        } else if (keyword.equals("many")) {
            return 4;
        } else {
            return 5;
        }
    }

}
