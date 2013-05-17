package com.android.i18n;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MessageFormatTest {
  public static void assertEquals(String lhs, String rhs) {
    if (lhs.equals(rhs)) {
      System.err.println("PASS: " + lhs + " == " + rhs);
    } else {
      System.err.println("FAIL: " + lhs + " != " + rhs);
    }
  }

  public static void main(String[] unused) throws Exception {
    MessageFormatTest t = new MessageFormatTest();
    t.testPluralRules();
    t.testJavaDocExamples();
    t.testPlural();
    t.testGender();
    t.testGenderAndPlural();
  }

  public void testPluralRules() throws Exception {
    assertEquals("keywords: [other, one] limit: 2 rules: one: [in, ints, 1]",
                 PluralRules.forLocale(Locale.US, PluralRules.PluralType.CARDINAL).toString());
    assertEquals("keywords: [two, other, one, few] limit: 101 " +
                 "rules: few: [mod: 10, in, ints, 3] && [mod: 100, except, ints, 13]; " +
                 "one: [mod: 10, in, ints, 1] && [mod: 100, except, ints, 11]; " +
                 "two: [mod: 10, in, ints, 2] && [mod: 100, except, ints, 12]",
                 PluralRules.forLocale(Locale.US, PluralRules.PluralType.ORDINAL).toString());
    assertEquals("keywords: [other] limit: 1 rules: (other)",
                 PluralRules.forLocale(Locale.KOREA, PluralRules.PluralType.CARDINAL).toString());
    assertEquals("keywords: [other] limit: 1 rules: (other)",
                 PluralRules.forLocale(Locale.KOREA, PluralRules.PluralType.ORDINAL).toString());
  }

  public void testJavaDocExamples() throws Exception {
    assertEquals("A 4:00:00 PM B Dec 31, 1969 C xxx D 7 E",
                 new MessageFormat("A {1,time} B {1,date} C {2} D {0,number,integer} E",
                                   Locale.US).format(7, new Date(0), "xxx"));
    assertEquals("A \"x\" B 3 C",
                 new MessageFormat("A \"{1}\" B {0} C", Locale.getDefault()).format(3, "x"));
  }

  public void testPlural() throws Exception {
    MessageFormat f = new MessageFormat("{0, plural, " +
                                        "=0{No files on \"{1}\".}" +
                                        "=1{One file on \"{1}\".}" +
                                        "other{# files on \"{1}\".}}", Locale.ENGLISH);
    assertEquals("No files on \"x\".",  f.format(0,   "x"));
    assertEquals("One file on \"x\".",  f.format(1,   "x"));
    assertEquals("123 files on \"x\".", f.format(123, "x"));
  }

  public void testGender() throws Exception {
    MessageFormat f = new MessageFormat("{0, select, " +
                                        "male{His thing.}" +
                                        "female{Her thing.}" +
                                        "other{Their thing.}}", Locale.ENGLISH);
    assertEquals("His thing.",   f.format("male"));
    assertEquals("Her thing.",   f.format("female"));
    assertEquals("Their thing.", f.format("other"));
  }

  public void testGenderAndPlural() throws Exception {
    MessageFormat f = new MessageFormat("{0, select, " +
                                        "male{His {1, plural, =1{thing}other{things}}.}" +
                                        "female{Her {1, plural, =1{thing}other{things}}.}" +
                                        "other{Their {1, plural, =1{thing}other{things}}.}}",
                                        Locale.ENGLISH);
    assertEquals("His thing.",    f.format("male",   1));
    assertEquals("Her thing.",    f.format("female", 1));
    assertEquals("Their thing.",  f.format("other",  1));
    assertEquals("His things.",   f.format("male",   2));
    assertEquals("Her things.",   f.format("female", 2));
    assertEquals("Their things.", f.format("other",  2));
  }
}
