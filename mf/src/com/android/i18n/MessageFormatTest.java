package com.android.i18n;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import dalvik.system.profiler.*;

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
    t.testPerformance();
  }

  public void testPerformance() throws Exception {
    /*
    SamplingProfiler.ThreadSet threadSet = SamplingProfiler.newArrayThreadSet(Thread.currentThread());
    SamplingProfiler profiler = new SamplingProfiler(12, threadSet);
    profiler.start(10);
    */

    long t0 = System.nanoTime();
    for (int i = 0; i < 1024*100; ++i) {
      MessageFormat.format(Locale.US,
                           "{0, plural, " +
                           "=0{No files on \"{1}\".}" +
                           "=1{One file on \"{1}\".}" +
                           "other{# files on \"{1}\".}}", 123, "x");

      MessageFormat.format(Locale.US, "A \"{1}\" B {0} C", 3, "x");

      MessageFormat.format(Locale.US,
                           "{0, select, " +
                           "male{His thing.}" +
                           "female{Her thing.}" +
                           "other{Their thing.}}", "female");
    }

    long t1 = System.nanoTime();
    System.err.println((t1 - t0)/100000);

    /*
    profiler.stop();
    profiler.shutdown();
    AsciiHprofWriter.write(profiler.getHprofData(), System.out);
    */
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
                 MessageFormat.format(Locale.US,
                                      "A {1,time} B {1,date} C {2} D {0,number,integer} E",
                                      7, new Date(0), "xxx"));
    assertEquals("A \"x\" B 3 C",
                 MessageFormat.format(Locale.US, "A \"{1}\" B {0} C", 3, "x"));
  }

  public void testPlural() throws Exception {
    String pattern = "{0, plural, " +
        "=0{No files on \"{1}\".}" +
        "=1{One file on \"{1}\".}" +
        "other{# files on \"{1}\".}}";
    assertEquals("No files on \"x\".",  MessageFormat.format(Locale.US, pattern, 0,   "x"));
    assertEquals("One file on \"x\".",  MessageFormat.format(Locale.US, pattern, 1,   "x"));
    assertEquals("123 files on \"x\".", MessageFormat.format(Locale.US, pattern, 123, "x"));
  }

  public void testGender() throws Exception {
    String pattern = "{0, select, " +
        "male{His thing.}" +
        "female{Her thing.}" +
        "other{Their thing.}}";
    assertEquals("His thing.",   MessageFormat.format(Locale.US, pattern, "male"));
    assertEquals("Her thing.",   MessageFormat.format(Locale.US, pattern, "female"));
    assertEquals("Their thing.", MessageFormat.format(Locale.US, pattern, "other"));
  }

  public void testGenderAndPlural() throws Exception {
    String pattern = "{0, select, " +
        "male{His {1, plural, =1{thing}other{things}}.}" +
        "female{Her {1, plural, =1{thing}other{things}}.}" +
        "other{Their {1, plural, =1{thing}other{things}}.}}";
    assertEquals("His thing.",    MessageFormat.format(Locale.US, pattern, "male",   1));
    assertEquals("Her thing.",    MessageFormat.format(Locale.US, pattern, "female", 1));
    assertEquals("Their thing.",  MessageFormat.format(Locale.US, pattern, "other",  1));
    assertEquals("His things.",   MessageFormat.format(Locale.US, pattern, "male",   2));
    assertEquals("Her things.",   MessageFormat.format(Locale.US, pattern, "female", 2));
    assertEquals("Their things.", MessageFormat.format(Locale.US, pattern, "other",  2));
  }
}
