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
    System.err.println(PluralRulesLoader.loader.forLocale(Locale.US, PluralRules.PluralType.CARDINAL));
    System.err.println(PluralRulesLoader.loader.forLocale(Locale.US, PluralRules.PluralType.ORDINAL));
    System.err.println(PluralRulesLoader.loader.forLocale(Locale.KOREA, PluralRules.PluralType.CARDINAL));
    System.err.println(PluralRulesLoader.loader.forLocale(Locale.KOREA, PluralRules.PluralType.ORDINAL));

    assertEquals("A 4:00:00 PM B Dec 31, 1969 C xxx D 7 E", MessageFormat.format("A {1,time} B {1,date} C {2} D {0,number,integer} E", new Object[] { 7, new Date(0), "xxx"}));

    assertEquals("A \"x\" B 3 C", new MessageFormat("A \"{1}\" B {0} C", Locale.getDefault()).format(new Object[] { 3, "x" }));

    MessageFormat f = new MessageFormat("{num_files, plural, " +
                                        "=0{No files on \"{disk_name}\".}" +
                                        "=1{One file on \"{disk_name}\".}" +
                                        "other{# files on \"{disk_name}\".}}", Locale.ENGLISH);
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put("num_files", 0);
    args.put("disk_name", "x");
    assertEquals("No files on \"x\".", f.format(args));

    args.put("num_files", 1);
    assertEquals("One file on \"x\".", f.format(args));

    args.put("num_files", 123);
    assertEquals("123 files on \"x\".", f.format(args));
  }
}
