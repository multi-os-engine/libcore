/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.util.Locale;

public class PluralRulesTest extends junit.framework.TestCase {
    public void testNegatives() throws Exception {
        // icu4c's behavior changed, but we prefer to preserve compatibility.
        PluralRules en_US = PluralRules.forLocale(new Locale("en", "US"));
        assertEquals(PluralRules.OTHER, en_US.quantityForInt(2));
        assertEquals(PluralRules.ONE, en_US.quantityForInt(1));
        assertEquals(PluralRules.OTHER, en_US.quantityForInt(0));
        assertEquals(PluralRules.OTHER, en_US.quantityForInt(-1));
        assertEquals(PluralRules.OTHER, en_US.quantityForInt(-2));

        PluralRules ar = PluralRules.forLocale(new Locale("ar"));
        assertEquals(PluralRules.ZERO, ar.quantityForInt(0));
        assertEquals(PluralRules.OTHER, ar.quantityForInt(-1)); // Not ONE.
        assertEquals(PluralRules.OTHER, ar.quantityForInt(-2)); // Not TWO.
        assertEquals(PluralRules.OTHER, ar.quantityForInt(-3)); // Not FEW.
        assertEquals(PluralRules.OTHER, ar.quantityForInt(-11)); // Not MANY.
        assertEquals(PluralRules.OTHER, ar.quantityForInt(-100));
    }

    public void testEnglish() throws Exception {
        PluralRules npr = PluralRules.forLocale(new Locale("en", "US"));
        assertEquals(PluralRules.OTHER, npr.quantityForInt(0));
        assertEquals(PluralRules.ONE, npr.quantityForInt(1));
        assertEquals(PluralRules.OTHER, npr.quantityForInt(2));
    }

    public void testCzech() throws Exception {
        PluralRules npr = PluralRules.forLocale(new Locale("cs", "CZ"));
        assertEquals(PluralRules.OTHER, npr.quantityForInt(0));
        assertEquals(PluralRules.ONE, npr.quantityForInt(1));
        assertEquals(PluralRules.FEW, npr.quantityForInt(2));
        assertEquals(PluralRules.FEW, npr.quantityForInt(3));
        assertEquals(PluralRules.FEW, npr.quantityForInt(4));
        assertEquals(PluralRules.OTHER, npr.quantityForInt(5));
    }

    public void testArabic() throws Exception {
        PluralRules npr = PluralRules.forLocale(new Locale("ar"));
        assertEquals(PluralRules.ZERO, npr.quantityForInt(0));
        assertEquals(PluralRules.ONE, npr.quantityForInt(1));
        assertEquals(PluralRules.TWO, npr.quantityForInt(2));
        for (int i = 3; i <= 10; ++i) {
            assertEquals(PluralRules.FEW, npr.quantityForInt(i));
        }
        assertEquals(PluralRules.MANY, npr.quantityForInt(11));
        assertEquals(PluralRules.MANY, npr.quantityForInt(99));
        assertEquals(PluralRules.OTHER, npr.quantityForInt(100));
        assertEquals(PluralRules.OTHER, npr.quantityForInt(101));
        assertEquals(PluralRules.OTHER, npr.quantityForInt(102));
        assertEquals(PluralRules.FEW, npr.quantityForInt(103));
        assertEquals(PluralRules.MANY, npr.quantityForInt(111));
    }

    public void testHebrew() throws Exception {
        // java.util.Locale will translate "he" to the deprecated "iw".
        PluralRules he = PluralRules.forLocale(new Locale("he"));
        assertEquals(PluralRules.ONE, he.quantityForInt(1));
        assertEquals(PluralRules.TWO, he.quantityForInt(2));
        assertEquals(PluralRules.OTHER, he.quantityForInt(3));
        assertEquals(PluralRules.OTHER, he.quantityForInt(10));
    }
}

