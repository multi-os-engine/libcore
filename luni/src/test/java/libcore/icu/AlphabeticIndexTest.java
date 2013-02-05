/*
 * Copyright (C) 2013 The Android Open Source Project
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

public class AlphabeticIndexTest extends junit.framework.TestCase {
  private static void assertHasLabel(Locale locale, String string, String expectedLabel) {
    AlphabeticIndex ai = new AlphabeticIndex(locale);
    ai.addLabels(Locale.US);
    int index = ai.getBucketIndex(string);
    String label = ai.getBucketLabel(index);
    assertEquals(expectedLabel, label);
  }

  public void test_en() throws Exception {
    // English [A-Z]
    assertHasLabel(Locale.ENGLISH, "Allen", "A");
    assertHasLabel(Locale.ENGLISH, "allen", "A");
  }

  public void test_ja() throws Exception {
    // Japanese
    //   sorts hiragana/katakana, Kanji/Chinese, English, other
    // …, あ, か, さ, た, な, は, ま, や, ら, わ, …
    // hiragana "a"
    assertHasLabel(Locale.JAPANESE, "Allen", "A");
    assertHasLabel(Locale.JAPANESE, "\u3041", "\u3042");
    // katakana "a"
    assertHasLabel(Locale.JAPANESE, "\u30a1", "\u3042");

    // Kanji (sorts to inflow section)
    assertHasLabel(Locale.JAPANESE, "\u65e5", "");

    // English
    assertHasLabel(Locale.JAPANESE, "Smith", "S");

    // Chinese (sorts to inflow section)
    assertHasLabel(Locale.JAPANESE, "\u6c88" /* Shen/Chen */, "");

    // Korean Hangul (sorts to overflow section)
    assertHasLabel(Locale.JAPANESE, "\u1100", "");
  }

  public void test_ko() throws Exception {
    // Korean (sorts Korean, then English)
    // …, ᄀ, ᄂ, ᄃ, ᄅ, ᄆ, ᄇ, ᄉ, ᄋ, ᄌ, ᄎ, ᄏ, ᄐ, ᄑ, ᄒ, …
    assertHasLabel(Locale.KOREAN, "\u1100", "\u1100");
    assertHasLabel(Locale.KOREAN, "\u3131", "\u1100");
    assertHasLabel(Locale.KOREAN, "\u1101", "\u1100");
    assertHasLabel(Locale.KOREAN, "\u1161", "\u1112");
  }

  public void test_cs() throws Exception {
    // Czech
    // …, [A-C], Č,[D-H], CH, [I-R], Ř, S, Š, [T-Z], Ž, …
    Locale cs = new Locale("cs");
    assertHasLabel(cs, "Cena", "C");
    assertHasLabel(cs, "Čáp", "\u010c");
    assertHasLabel(cs, "Ruda", "R");
    assertHasLabel(cs, "Řada", "\u0158");
    assertHasLabel(cs, "Selka", "S");
    assertHasLabel(cs, "Šála", "\u0160");
    assertHasLabel(cs, "Zebra", "Z");
    assertHasLabel(cs, "Žába", "\u017d");
    assertHasLabel(cs, "Chata", "CH");
  }

  public void test_fr() throws Exception {
    // French: [A-Z] (no accented chars)
    assertHasLabel(Locale.FRENCH, "Øfer", "O");
    assertHasLabel(Locale.FRENCH, "Œster", "O");
  }

  public void test_da() throws Exception {
    // Danish: [A-Z], Æ, Ø, Å
    Locale da = new Locale("da");
    assertHasLabel(da, "Ænes", "\u00c6");
    assertHasLabel(da, "Øfer", "\u00d8");
    assertHasLabel(da, "Œster", "\u00d8");
    assertHasLabel(da, "Ågård", "\u00c5");
  }

  public void test_de() throws Exception {
    // German: [A-Z] (no ß or umlauted characters in standard alphabet)
    assertHasLabel(Locale.GERMAN, "ßind", "S");
  }

  public void test_th() throws Exception {
    // Thai (sorts English then Thai)
    // …, ก, ข, ฃ, ค, ฅ, ฆ, ง, จ, ฉ, ช, ซ, ฌ, ญ, ฎ, ฏ, ฐ, ฑ, ฒ, ณ, ด, ต, ถ, ท, ธ, น, บ, ป, ผ, ฝ, พ, ฟ, ภ, ม, ย, ร, ฤ, ล, ฦ, ว, ศ, ษ, ส, ห, ฬ, อ, ฮ, …,
    Locale th = new Locale("th");
    assertHasLabel(th, "\u0e2d\u0e07\u0e04\u0e4c\u0e40\u0e25\u0e47\u0e01", "\u0e2d");
    assertHasLabel(th, "\u0e2a\u0e34\u0e07\u0e2b\u0e40\u0e2a\u0e19\u0e35", "\u0e2a");
  }

  public void test_ar() throws Exception {
    // Arabic (sorts English then Arabic)
    // …, ا, ب, ت, ث, ج, ح, خ, د, ذ, ر, ز, س, ش, ص, ض, ط, ظ, ع, غ, ف, ق, ك, ل, م, ن, ه, و, ي, …
    Locale ar = new Locale("ar");
    assertHasLabel(ar, "\u0646\u0648\u0631", /* Noor */ "\u0646");
  }

  public void test_he() throws Exception {
    // Hebrew (sorts English then Hebrew)
    // …, א, ב, ג, ד, ה, ו, ז, ח, ט, י, כ, ל, מ, נ, ס, ע, פ, צ, ק, ר, ש, ת, …
    Locale he = new Locale("he");
    assertHasLabel(he, "\u05e4\u05e8\u05d9\u05d3\u05de\u05df", "\u05e4");
  }

  public void test_zh_CN() throws Exception {
    // Simplified Chinese (default collator Pinyin): [A-Z]
    // Shen/Chen (simplified): should be, usually, 'S' for name collator and 'C' for apps/other
    Locale zh_CN = new Locale("zh", "CN");

    // Shen/Chen
    assertHasLabel(zh_CN, "\u6c88", "S");
    // Shen/Chen (traditional)
    assertHasLabel(zh_CN, "\u700b", "S");

    // Jia/Gu: should be, usually, 'J' for name collator and 'G' for apps/other
    assertHasLabel(zh_CN, "\u8d3e", "J");
  }

  public void test_zh_TW() throws Exception {
    // Traditional Chinese
    // …, 一, 丁, 丈, 不, 且, 丞, 串, 並, 亭, 乘, 乾, 傀, 亂, 僎, 僵, 儐, 償, 叢, 儳, 嚴, 儷, 儻, 囌, 囑, 廳, …
    // Shen/Chen
    Locale zh_TW = new Locale("zh", "TW");
    assertHasLabel(zh_TW, "\u6c88", "\u5080");
    assertHasLabel(zh_TW, "\u700b", "\u53e2");
    // Jia/Gu
    assertHasLabel(zh_TW, "\u8d3e", "\u5080");
  }

  public void test_constructor_NPE() throws Exception {
    try {
      new AlphabeticIndex(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void test_addLabels_NPE() throws Exception {
    AlphabeticIndex ai = new AlphabeticIndex(Locale.US);
    try {
      ai.addLabels(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void test_getBucketIndex_NPE() throws Exception {
    AlphabeticIndex ai = new AlphabeticIndex(Locale.US);
    try {
      ai.getBucketIndex(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void test_getBucketLabel_invalid() throws Exception {
    AlphabeticIndex ai = new AlphabeticIndex(Locale.US);
    try {
      ai.getBucketLabel(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      ai.getBucketLabel(123456);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }
}
