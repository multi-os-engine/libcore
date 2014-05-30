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

import java.util.Arrays;
import java.util.Locale;

public class ICUTest extends junit.framework.TestCase {
  public void test_getISOLanguages() throws Exception {
    // Check that corrupting our array doesn't affect other callers.
    assertNotNull(ICU.getISOLanguages()[0]);
    ICU.getISOLanguages()[0] = null;
    assertNotNull(ICU.getISOLanguages()[0]);
  }

  public void test_getISOCountries() throws Exception {
    // Check that corrupting our array doesn't affect other callers.
    assertNotNull(ICU.getISOCountries()[0]);
    ICU.getISOCountries()[0] = null;
    assertNotNull(ICU.getISOCountries()[0]);
  }

  public void test_getAvailableLocales() throws Exception {
    // Check that corrupting our array doesn't affect other callers.
    assertNotNull(ICU.getAvailableLocales()[0]);
    ICU.getAvailableLocales()[0] = null;
    assertNotNull(ICU.getAvailableLocales()[0]);
  }

  public void test_getBestDateTimePattern() throws Exception {
    assertEquals("d MMMM", ICU.getBestDateTimePattern("MMMMd", "ca_ES"));
    assertEquals("d 'de' MMMM", ICU.getBestDateTimePattern("MMMMd", "es_ES"));
    assertEquals("d. MMMM", ICU.getBestDateTimePattern("MMMMd", "de_CH"));
    assertEquals("MMMM d", ICU.getBestDateTimePattern("MMMMd", "en_US"));
    assertEquals("d LLLL", ICU.getBestDateTimePattern("MMMMd", "fa_IR"));
    assertEquals("M月d日", ICU.getBestDateTimePattern("MMMMd", "ja_JP"));
  }

  public void test_localeFromString() throws Exception {
    // localeFromString is pretty lenient. Some of these can't be round-tripped
    // through Locale.toString.
    assertEquals(Locale.ENGLISH, ICU.localeFromIcuLocaleId("en"));
    assertEquals(Locale.ENGLISH, ICU.localeFromIcuLocaleId("en_"));
    assertEquals(Locale.ENGLISH, ICU.localeFromIcuLocaleId("en__"));
    assertEquals(Locale.US, ICU.localeFromIcuLocaleId("en_US"));
    assertEquals(Locale.US, ICU.localeFromIcuLocaleId("en_US_"));
    assertEquals(new Locale("", "US", ""), ICU.localeFromIcuLocaleId("_US"));
    assertEquals(new Locale("", "US", ""), ICU.localeFromIcuLocaleId("_US_"));
    assertEquals(new Locale("", "", "POSIX"), ICU.localeFromIcuLocaleId("__POSIX"));
    assertEquals(new Locale("aa", "BB", "CC"), ICU.localeFromIcuLocaleId("aa_BB_CC"));
  }

  public void test_getScript_addLikelySubtags() throws Exception {
    assertEquals("Latn", ICU.getScript(ICU.addLikelySubtags("en_US")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("he")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("he_IL")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("iw")));
    assertEquals("Hebr", ICU.getScript(ICU.addLikelySubtags("iw_IL")));
  }

  private String best(Locale l, String skeleton) {
    return ICU.getBestDateTimePattern(skeleton, l.toString());
  }

  public void test_getDateFormatOrder() throws Exception {
    // lv and fa use differing orders depending on whether you're using numeric or textual months.
    Locale lv = new Locale("lv");
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(lv, "yyyy-M-dd"))));
    assertEquals("[y, d, M]", Arrays.toString(ICU.getDateFormatOrder(best(lv, "yyyy-MMM-dd"))));
    assertEquals("[d, M, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(lv, "MMM-dd"))));
    Locale fa = new Locale("fa");
    assertEquals("[y, M, d]", Arrays.toString(ICU.getDateFormatOrder(best(fa, "yyyy-M-dd"))));
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(fa, "yyyy-MMM-dd"))));
    assertEquals("[d, M, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(fa, "MMM-dd"))));

    // English differs on each side of the Atlantic.
    Locale en_US = Locale.US;
    assertEquals("[M, d, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_US, "yyyy-M-dd"))));
    assertEquals("[M, d, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_US, "yyyy-MMM-dd"))));
    assertEquals("[M, d, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(en_US, "MMM-dd"))));
    Locale en_GB = Locale.UK;
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_GB, "yyyy-M-dd"))));
    assertEquals("[d, M, y]", Arrays.toString(ICU.getDateFormatOrder(best(en_GB, "yyyy-MMM-dd"))));
    assertEquals("[d, M, \u0000]", Arrays.toString(ICU.getDateFormatOrder(best(en_GB, "MMM-dd"))));

    assertEquals("[y, M, d]", Arrays.toString(ICU.getDateFormatOrder("yyyy - 'why' '' 'ddd' MMM-dd")));

    try {
      ICU.getDateFormatOrder("the quick brown fox jumped over the lazy dog");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      ICU.getDateFormatOrder("'");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      ICU.getDateFormatOrder("yyyy'");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      ICU.getDateFormatOrder("yyyy'MMM");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testScriptsPassedToIcu() throws Exception {
    Locale sr_Cyrl_BA = Locale.forLanguageTag("sr-Cyrl-BA");
    Locale sr_Cyrl_ME = Locale.forLanguageTag("sr-Cyrl-ME");
    Locale sr_Latn_BA = Locale.forLanguageTag("sr-Latn-BA");
    Locale sr_Latn_ME = Locale.forLanguageTag("sr-Latn-ME");

    assertEquals("sr_BA_#Cyrl", sr_Cyrl_BA.toString());
    assertEquals("Cyrl",        sr_Cyrl_BA.getScript());

    assertEquals("sr_ME_#Cyrl", sr_Cyrl_ME.toString());
    assertEquals("Cyrl",        sr_Cyrl_ME.getScript());

    assertEquals("sr_BA_#Latn", sr_Latn_BA.toString());
    assertEquals("Latn",        sr_Latn_BA.getScript());

    assertEquals("sr_ME_#Latn", sr_Latn_ME.toString());
    assertEquals("Latn",        sr_Latn_ME.getScript());

    assertEquals("Српски",              sr_Cyrl_BA.getDisplayLanguage(sr_Cyrl_BA));
    assertEquals("Босна и Херцеговина", sr_Cyrl_BA.getDisplayCountry(sr_Cyrl_BA));
    assertEquals("Ћирилица",            sr_Cyrl_BA.getDisplayScript(sr_Cyrl_BA));

    assertEquals("Српски",    sr_Cyrl_ME.getDisplayLanguage(sr_Cyrl_ME));
    assertEquals("Црна Гора", sr_Cyrl_ME.getDisplayCountry(sr_Cyrl_ME));
    assertEquals("Ћирилица",  sr_Cyrl_ME.getDisplayScript(sr_Cyrl_ME));

    assertEquals("Srpski",              sr_Latn_BA.getDisplayLanguage(sr_Latn_BA));
    assertEquals("Bosna i Hercegovina", sr_Latn_BA.getDisplayCountry(sr_Latn_BA));
    assertEquals("Latinica",            sr_Latn_BA.getDisplayScript(sr_Latn_BA));

    assertEquals("Srpski",    sr_Latn_ME.getDisplayLanguage(sr_Latn_ME));
    assertEquals("Crna Gora", sr_Latn_ME.getDisplayCountry(sr_Latn_ME));
    assertEquals("Latinica",  sr_Latn_ME.getDisplayScript(sr_Latn_ME));
  }
}
