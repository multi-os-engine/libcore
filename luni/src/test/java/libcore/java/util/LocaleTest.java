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

package libcore.java.util;

import java.text.BreakIterator;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

public class LocaleTest extends junit.framework.TestCase {
    // http://b/2611311; if there's no display language/country/variant, use the raw codes.
    public void test_getDisplayName_invalid() throws Exception {
        Locale invalid = new Locale("AaBbCc", "DdEeFf", "GgHhIi");

        assertEquals("aabbcc", invalid.getLanguage());
        assertEquals("DDEEFF", invalid.getCountry());
        assertEquals("GgHhIi", invalid.getVariant());

        // Android using icu4c < 49.2 returned empty strings for display language, country,
        // and variant, but a display name made up of the raw strings.
        // Newer releases return slightly different results, but no less unreasonable.
        assertEquals("aabbcc", invalid.getDisplayLanguage());
        assertEquals("", invalid.getDisplayCountry());
        assertEquals("DDEEFF_GGHHII", invalid.getDisplayVariant());
        assertEquals("aabbcc (DDEEFF,DDEEFF_GGHHII)", invalid.getDisplayName());
    }

    // http://b/2611311; if there's no display language/country/variant, use the raw codes.
    public void test_getDisplayName_unknown() throws Exception {
        Locale unknown = new Locale("xx", "YY", "Traditional");
        assertEquals("xx", unknown.getLanguage());
        assertEquals("YY", unknown.getCountry());
        assertEquals("Traditional", unknown.getVariant());

        assertEquals("xx", unknown.getDisplayLanguage());
        assertEquals("YY", unknown.getDisplayCountry());
        assertEquals("TRADITIONAL", unknown.getDisplayVariant());
        assertEquals("xx (YY,TRADITIONAL)", unknown.getDisplayName());
    }

    public void test_getDisplayName_easy() throws Exception {
        assertEquals("English", Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH));
        assertEquals("German", Locale.GERMAN.getDisplayLanguage(Locale.ENGLISH));
        assertEquals("Englisch", Locale.ENGLISH.getDisplayLanguage(Locale.GERMAN));
        assertEquals("Deutsch", Locale.GERMAN.getDisplayLanguage(Locale.GERMAN));
    }

    public void test_getDisplayCountry_8870289() throws Exception {
        assertEquals("Hong Kong", new Locale("", "HK").getDisplayCountry(Locale.US));
        assertEquals("Macau", new Locale("", "MO").getDisplayCountry(Locale.US));
        assertEquals("Palestine", new Locale("", "PS").getDisplayCountry(Locale.US));

        assertEquals("Cocos [Keeling] Islands", new Locale("", "CC").getDisplayCountry(Locale.US));
        assertEquals("Congo [DRC]", new Locale("", "CD").getDisplayCountry(Locale.US));
        assertEquals("Congo [Republic]", new Locale("", "CG").getDisplayCountry(Locale.US));
        assertEquals("Falkland Islands [Islas Malvinas]", new Locale("", "FK").getDisplayCountry(Locale.US));
        assertEquals("Macedonia [FYROM]", new Locale("", "MK").getDisplayCountry(Locale.US));
        assertEquals("Myanmar [Burma]", new Locale("", "MM").getDisplayCountry(Locale.US));
        assertEquals("Taiwan", new Locale("", "TW").getDisplayCountry(Locale.US));
    }

    public void test_tl() throws Exception {
        // In jb-mr1, we had a last-minute hack to always return "Filipino" because
        // icu4c 4.8 didn't have any localizations for fil. (http://b/7291355)
        Locale tl = new Locale("tl");
        Locale tl_PH = new Locale("tl", "PH");
        assertEquals("Filipino", tl.getDisplayLanguage(Locale.ENGLISH));
        assertEquals("Filipino", tl_PH.getDisplayLanguage(Locale.ENGLISH));
        assertEquals("Filipino", tl.getDisplayLanguage(tl));
        assertEquals("Filipino", tl_PH.getDisplayLanguage(tl_PH));

        // After the icu4c 4.9 upgrade, we could localize "fil" correctly, though we
        // needed another hack to supply "fil" instead of "tl" to icu4c. (http://b/8023288)
        Locale es_MX = new Locale("es", "MX");
        assertEquals("filipino", tl.getDisplayLanguage(es_MX));
        assertEquals("filipino", tl_PH.getDisplayLanguage(es_MX));
      }

    // http://b/3452611; Locale.getDisplayLanguage fails for the obsolete language codes.
    public void test_getDisplayName_obsolete() throws Exception {
        // he (new) -> iw (obsolete)
        assertObsolete("he", "iw", "עברית");
        // id (new) -> in (obsolete)
        assertObsolete("id", "in", "Bahasa Indonesia");
    }

    private static void assertObsolete(String newCode, String oldCode, String displayName) {
        // Either code should get you the same locale.
        Locale newLocale = new Locale(newCode);
        Locale oldLocale = new Locale(oldCode);
        assertEquals(newLocale, oldLocale);

        // No matter what code you used to create the locale, you should get the old code back.
        assertEquals(oldCode, newLocale.getLanguage());
        assertEquals(oldCode, oldLocale.getLanguage());

        // Check we get the right display name.
        assertEquals(displayName, newLocale.getDisplayLanguage(newLocale));
        assertEquals(displayName, oldLocale.getDisplayLanguage(newLocale));
        assertEquals(displayName, newLocale.getDisplayLanguage(oldLocale));
        assertEquals(displayName, oldLocale.getDisplayLanguage(oldLocale));

        // Check that none of the 'getAvailableLocales' methods are accidentally returning two
        // equal locales (because to ICU they're different, but we mangle one into the other).
        assertOnce(newLocale, BreakIterator.getAvailableLocales());
        assertOnce(newLocale, Calendar.getAvailableLocales());
        assertOnce(newLocale, Collator.getAvailableLocales());
        assertOnce(newLocale, DateFormat.getAvailableLocales());
        assertOnce(newLocale, DateFormatSymbols.getAvailableLocales());
        assertOnce(newLocale, NumberFormat.getAvailableLocales());
        assertOnce(newLocale, Locale.getAvailableLocales());
    }

    private static void assertOnce(Locale element, Locale[] array) {
        int count = 0;
        for (Locale l : array) {
            if (l.equals(element)) {
                ++count;
            }
        }
        assertEquals(1, count);
    }

    public void test_getISO3Country() {
        // Empty country code.
        assertEquals("", new Locale("en", "").getISO3Country());

        // Invalid country code.
        try {
            assertEquals("", new Locale("en", "XX").getISO3Country());
            fail();
        } catch (MissingResourceException expected) {
            assertEquals("FormatData_en_XX", expected.getClassName());
            assertEquals("ShortCountry", expected.getKey());
        }

        // Valid country code.
        assertEquals("CAN", new Locale("", "CA").getISO3Country());
        assertEquals("CAN", new Locale("en", "CA").getISO3Country());
        assertEquals("CAN", new Locale("xx", "CA").getISO3Country());
    }

    public void test_getISO3Language() {
        // Empty language code.
        assertEquals("", new Locale("", "US").getISO3Language());

        // Invalid language code.
        try {
            assertEquals("", new Locale("xx", "US").getISO3Language());
            fail();
        } catch (MissingResourceException expected) {
            assertEquals("FormatData_xx_US", expected.getClassName());
            assertEquals("ShortLanguage", expected.getKey());
        }

        // Valid language code.
        assertEquals("eng", new Locale("en", "").getISO3Language());
        assertEquals("eng", new Locale("en", "CA").getISO3Language());
        assertEquals("eng", new Locale("en", "XX").getISO3Language());
    }

    public void test_serializeExtensions() {
        Map<Character, String> extensions = new TreeMap<Character, String>();

        extensions.put('x', "fooo-baar-baaz");
        assertEquals("x-fooo-baar-baaz", Locale.serializeExtensions(extensions));

        extensions.put('y', "gaaa-caar-caaz");
        // Must show up in lexical order.
        assertEquals("x-fooo-baar-baaz-y-gaaa-caar-caaz",
                Locale.serializeExtensions(extensions));
    }

    public void test_parseSerializedExtensions() {
        Map<Character, String> extensions = new HashMap<Character, String>();

        Locale.parseSerializedExtensions("x-foo", extensions);
        assertEquals("foo", extensions.get('x'));

        extensions.clear();
        Locale.parseSerializedExtensions("x-foo-y-bar-z-baz", extensions);
        assertEquals("foo", extensions.get('x'));
        assertEquals("bar", extensions.get('y'));
        assertEquals("baz", extensions.get('z'));

        extensions.clear();
        Locale.parseSerializedExtensions("x-fooo-baar-baaz", extensions);
        assertEquals("fooo-baar-baaz", extensions.get('x'));

        extensions.clear();
        Locale.parseSerializedExtensions("x-fooo-baar-baaz-y-gaaa-caar-caaz", extensions);
        assertEquals("fooo-baar-baaz", extensions.get('x'));
        assertEquals("gaaa-caar-caaz", extensions.get('y'));
    }

    public void test_parseUnicodeExtension() {
        Map<String, String> keywords = new HashMap<String, String>();
        Set<String> attributes = new HashSet<String>();

        // Only attributes.
        Locale.parseUnicodeExtension("foooo".split("-"), keywords, attributes);
        assertTrue(attributes.contains("foooo"));
        assertTrue(keywords.isEmpty());

        attributes.clear();
        keywords.clear();
        Locale.parseUnicodeExtension("foooo-baa-baaabaaa".split("-"),
                keywords, attributes);
        assertTrue(attributes.contains("foooo"));
        assertTrue(attributes.contains("baa"));
        assertTrue(attributes.contains("baaabaaa"));
        assertTrue(keywords.isEmpty());

        // Only keywords
        attributes.clear();
        keywords.clear();
        Locale.parseUnicodeExtension("ko-koko".split("-"), keywords, attributes);
        assertTrue(attributes.isEmpty());
        assertEquals("koko", keywords.get("ko"));

        attributes.clear();
        keywords.clear();
        Locale.parseUnicodeExtension("ko-koko-kokoko".split("-"), keywords, attributes);
        assertTrue(attributes.isEmpty());
        assertEquals("koko-kokoko", keywords.get("ko"));

        attributes.clear();
        keywords.clear();
        Locale.parseUnicodeExtension("ko-koko-kokoko-ba-baba-bababa".split("-"),
                keywords, attributes);
        assertTrue(attributes.isEmpty());
        assertEquals("koko-kokoko", keywords.get("ko"));
        assertEquals("baba-bababa", keywords.get("ba"));

        // A mixture of attributes and keywords.
        attributes.clear();
        keywords.clear();
        Locale.parseUnicodeExtension("attri1-attri2-k1-type1-type1-k2-type2".split("-"),
                keywords, attributes);
        assertTrue(attributes.contains("attri1"));
        assertTrue(attributes.contains("attri2"));
        assertEquals("type1-type1", keywords.get("k1"));
        assertEquals("type2", keywords.get("k2"));
    }

    public void test_Builder_setLanguage() {
        Locale.Builder b = new Locale.Builder();

        // Should normalize to lower case.
        b.setLanguage("EN");
        assertEquals("en", b.build().getLanguage());

        b = new Locale.Builder();

        // Too short.
        try {
            b.setLanguage("e");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Too long
        try {
            b.setLanguage("englyishh");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Contains non ASCII characters
        try {
            b.setLanguage("தமிழ்");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Null or empty languages must clear state.
        b = new Locale.Builder();
        b.setLanguage("en");
        b.setLanguage(null);
        assertEquals("", b.build().getLanguage());

        b = new Locale.Builder();
        b.setLanguage("en");
        b.setLanguage("");
        assertEquals("", b.build().getLanguage());
    }

    public void test_Builder_setRegion() {
        Locale.Builder b = new Locale.Builder();

        // Should normalize to upper case.
        b.setRegion("us");
        assertEquals("US", b.build().getCountry());

        b = new Locale.Builder();

        // Too short.
        try {
            b.setRegion("e");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Too long
        try {
            b.setRegion("USA");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Contains non ASCII characters
        try {
            b.setLanguage("திழ்");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Null or empty regions must clear state.
        b = new Locale.Builder();
        b.setRegion("US");
        b.setRegion(null);
        assertEquals("", b.build().getCountry());

        b = new Locale.Builder();
        b.setRegion("US");
        b.setRegion("");
        assertEquals("", b.build().getCountry());
    }

    public void test_Builder_setVariant() {
        Locale.Builder b = new Locale.Builder();

        // Should normalize variants to lower case.
        b.setVariant("vArIaNt-VaRiAnT");
        assertEquals("variant_variant", b.build().getVariant());

        // And normalize "_" to "-"
        b = new Locale.Builder();
        b.setVariant("vArIaNt-VaRiAnT-VARIANT");
        assertEquals("variant_variant_variant", b.build().getVariant());

        b = new Locale.Builder();
        // Too short
        try {
            b.setVariant("shor");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Too long
        try {
            b.setVariant("waytoolong");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        try {
            b.setVariant("foooo-foooo-fo");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Special case. Variants of length 4 are allowed when the first
        // character is a digit.
        b.setVariant("0ABC");
        assertEquals("0abc", b.build().getVariant());

        b = new Locale.Builder();
        b.setVariant("variant");
        b.setVariant(null);
        assertEquals("", b.build().getVariant());

        b = new Locale.Builder();
        b.setVariant("variant");
        b.setVariant("");
        assertEquals("", b.build().getVariant());
    }

    public void test_Builder_setLocale() {
        // Default case.
        Locale.Builder b = new Locale.Builder();
        b.setLocale(Locale.US);
        assertEquals("en", b.build().getLanguage());
        assertEquals("US", b.build().getCountry());

        // Should throw when locale is malformed.
        // - Bad language
        Locale bad = new Locale("e", "US");
        b = new Locale.Builder();
        try {
            b.setLocale(bad);
            fail();
        } catch (IllformedLocaleException expected) {
        }
        // - Bad country
        bad = new Locale("en", "USA");
        try {
            b.setLocale(bad);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // - Bad variant
        bad = new Locale("en", "US", "c");
        try {
            b.setLocale(bad);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Test values are normalized as they should be
        b = new Locale.Builder();
        Locale good = new Locale("EN", "us", "variant-vARIANT");
        b.setLocale(good);
        Locale l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());
        assertEquals("variant_variant", l.getVariant());

        // Test that none of the existing fields are messed with
        // if the locale update fails.
        b = new Locale.Builder();
        b.setLanguage("fr").setRegion("FR");

        try {
            b.setLocale(bad);
            fail();
        } catch (IllformedLocaleException expected) {
        }

        l = b.build();
        assertEquals("fr", l.getLanguage());
        assertEquals("FR", l.getCountry());
    }

    public void test_Builder_setScript() {
        Locale.Builder b = new Locale.Builder();

        // Should normalize variants to lower case.
        b.setScript("lAtN");
        assertEquals("Latn", b.build().getScript());

        b = new Locale.Builder();
        // Too short
        try {
            b.setScript("lat");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Too long
        try {
            b.setScript("latin");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        b = new Locale.Builder();
        b.setScript("Latn");
        b.setScript(null);
        assertEquals("", b.build().getScript());

        b = new Locale.Builder();
        b.setScript("Latn");
        b.setScript("");
        assertEquals("", b.build().getScript());
    }

    public void test_Builder_clear() {
        Locale.Builder b = new Locale.Builder();
        b.setLanguage("en").setScript("Latn").setRegion("US")
                .setVariant("POSIX").setExtension('g', "foo")
                .setUnicodeLocaleKeyword("fo", "baar")
                .addUnicodeLocaleAttribute("baaaaz");

        Locale l = b.clear().build();
        assertEquals("", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getVariant());
        assertEquals("", l.getScript());
        assertTrue(l.getExtensionKeys().isEmpty());
    }

    public void test_Builder_setExtension() {
        Locale.Builder b = new Locale.Builder();
        b.setExtension('g', "FO_ba-BR_bg");

        Locale l = b.build();
        assertEquals("fo-ba-br-bg", l.getExtension('g'));

        b = new Locale.Builder();

        // Too short
        try {
            b.setExtension('g', "fo-ba-br-x");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Too long
        try {
            b.setExtension('g', "fo-ba-br-extension");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // Special case, the private use extension allows single char subtags.
        b.setExtension(Locale.PRIVATE_USE_EXTENSION, "fo-ba-br-m");
        l = b.build();
        assertEquals("fo-ba-br-m", l.getExtension('x'));

        // Special case, the unicode locale extension must be parsed into
        // its individual components. The correctness of the parse is tested
        // in test_parseUnicodeExtension.
        b.setExtension(Locale.UNICODE_LOCALE_EXTENSION, "foooo_BaaaR-BA_Baz-bI_BIZ");
        l = b.build();
        // Note that attributes and keywords are sorted alphabetically.
        assertEquals("baaar-foooo-ba-baz-bi-biz", l.getExtension('u'));

        assertTrue(l.getUnicodeLocaleAttributes().contains("foooo"));
        assertTrue(l.getUnicodeLocaleAttributes().contains("baaar"));
        assertEquals("baz", l.getUnicodeLocaleType("ba"));
        assertEquals("biz", l.getUnicodeLocaleType("bi"));
    }

    public void test_Builder_clearExtensions() {
        Locale.Builder b = new Locale.Builder();
        b.setExtension('g', "FO_ba-BR_bg");
        b.setExtension(Locale.PRIVATE_USE_EXTENSION, "fo-ba-br-m");
        b.clearExtensions();

        assertTrue(b.build().getExtensionKeys().isEmpty());
    }

    public void test_Builder_setLanguageTag_singleSubtag() {
        Locale.Builder b = new Locale.Builder();
        b.setLanguageTag("en");

        Locale l = b.build();
        assertEquals("en", l.getLanguage());

        b = new Locale.Builder();
        b.setLanguageTag("eng");
        l = b.build();
        assertEquals("eng", l.getLanguage());

        b = new Locale.Builder();
        try {
            b.setLanguageTag("a");
            fail();
        } catch (IllformedLocaleException ifle) {
        }
    }

    public void test_Builder_setLanguageTag_twoSubtags() {
        Locale.Builder b = new Locale.Builder();
        b.setLanguageTag("en-US");

        Locale l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());

        b = new Locale.Builder();
        b.setLanguageTag("eng-419");
        l = b.build();
        assertEquals("eng", l.getLanguage());
        assertEquals("419", l.getCountry());

        // IND is an invalid region code so ICU helpfully tries to parse it as
        // a 3 letter language code, even if it isn't a valid ISO-639-3 code
        // either.
        b = new Locale.Builder();
        b.setLanguageTag("en-USB");
        l = b.build();
        assertEquals("usb", l.getLanguage());
        assertEquals("", l.getCountry());

        // Script tags shouldn't be mis-recognized as regions.
        b = new Locale.Builder();
        b.setLanguageTag("en-Latn");
        l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("Latn", l.getScript());

        // Neither should variant tags.
        b = new Locale.Builder();
        b.setLanguageTag("en-POSIX");
        l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getScript());
    }

    public void test_Builder_setLanguageTag_threeSubtags() {
        // lang-region-variant
        Locale.Builder b = new Locale.Builder();
        b.setLanguageTag("en-US-FOOOO");
        Locale l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());
        assertEquals("", l.getScript());
        assertEquals("foooo", l.getVariant());

        // lang-script-variant
        b = new Locale.Builder();
        b.setLanguageTag("en-Latn-FOOOO");
        l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("Latn", l.getScript());
        assertEquals("foooo", l.getVariant());

        // lang-script-region
        b = new Locale.Builder();
        b.setLanguageTag("en-Latn-US");
        l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("US", l.getCountry());
        assertEquals("Latn", l.getScript());
        assertEquals("", l.getVariant());

        // lang-variant-variant
        b = new Locale.Builder();
        b.setLanguageTag("en-FOOOO-BAAAAR");
        l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getCountry());
        assertEquals("", l.getScript());
        assertEquals("foooo_baaaar", l.getVariant());

        // lang-region-illformedvariant
        b = new Locale.Builder();
        try {
            b.setLanguageTag("en-US-BA");
            fail();
        } catch (IllformedLocaleException expected) {
        }

        // lang-variant-illformedvariant
        try {
            b.setLanguageTag("en-FOOOO-BA");
            fail();
        } catch (IllformedLocaleException expected) {
        }
    }

    public void test_Builder_setLanguageTag_fourOrMoreSubtags() {
        Locale.Builder b = new Locale.Builder();
        b.setLanguageTag("en-Latn-US-foooo");

        // Single variant.
        Locale l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("US", l.getCountry());
        assertEquals("foooo", l.getVariant());

        // Variant with multiple subtags.
        b = new Locale.Builder();
        b.setLanguageTag("en-Latn-US-foooo-gfffh");
        l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("US", l.getCountry());
        assertEquals("foooo_gfffh", l.getVariant());

        // Variant with 2 subtags. POSIX shouldn't be recognized
        // as a region or a script.
        b = new Locale.Builder();
        b.setLanguageTag("en-POSIX-P2003");
        l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getScript());
        assertEquals("", l.getCountry());
        assertEquals("posix_p2003", l.getVariant());

        // Variant with 3 subtags. POSIX shouldn't be recognized
        // as a region or a script.
        b = new Locale.Builder();
        b.setLanguageTag("en-POSIX-P2003-P2004");
        l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("", l.getScript());
        assertEquals("", l.getCountry());
        assertEquals("posix_p2003_p2004", l.getVariant());

        b = new Locale.Builder();
        b.setLanguageTag("en-Latn-POSIX-P2003");
        l = b.build();
        assertEquals("en", l.getLanguage());
        assertEquals("Latn", l.getScript());
        assertEquals("", l.getCountry());
        assertEquals("posix_p2003", l.getVariant());
    }

    public void test_forLanguageTag() {

    }

    public void test_getDisplayScript() {
        Locale.Builder b = new Locale.Builder();
        b.setLanguage("en").setRegion("US").setScript("Latn");

        Locale l = b.build();
        assertEquals("Latin", l.getDisplayScript());
        assertEquals("Lateinisch", l.getDisplayScript(Locale.GERMAN));
        // Fallback for navajo, a language for which we don't have data.
        assertEquals("Latin", l.getDisplayScript(new Locale("nv", "US")));

        b= new Locale.Builder();
        b.setLanguage("en").setRegion("US").setScript("Fooo");

        // Will be equivalent to getScriptCode for scripts that aren't
        // registered with ISO-15429 (but are otherwise well formed).
        l = b.build();
        assertEquals("Fooo", l.getDisplayScript());
    }


    public void test_Builder_unicodeExtensions() {

    }

    public void test_getExtension() {

    }

    public void test_unicodeLocaleExtensions() {

    }

    public void test_toLanguageTag() {

    }

    public void test_toString() {

    }

    public void test_serializedForm() {

    }
}

