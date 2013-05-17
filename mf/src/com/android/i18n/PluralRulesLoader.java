package com.android.i18n;

import java.text.ParseException;
import java.util.ListResourceBundle;
import java.util.Locale;

/**
 * Loader for plural rules data.
 */
public class PluralRulesLoader {
  public static final PluralRulesLoader loader = new PluralRulesLoader();

  private PluralRulesLoader() {
  }

  // TODO: cache.
  public PluralRules forLocale(Locale locale, PluralRules.PluralType type) {
    ListResourceBundle bundle = new LocaleElements_plurals();
    String key = getRuleSetForLanguage(bundle, locale.getLanguage(), type);
    if (key.isEmpty()) {
      return PluralRules.DEFAULT;
    }

    Object[][] allRules = (Object[][]) bundle.getObject("rules");
    for (int i = 0; i < allRules.length; ++i) {
      if (allRules[i][0].equals(key)) {
        return makePluralRules((Object[][]) allRules[i][1]);
      }
    }

    throw new AssertionError("couldn't find " + key + " for " + locale);
  }

  private String getRuleSetForLanguage(ListResourceBundle bundle, String languageCode, PluralRules.PluralType type) {
    String key = (type == PluralRules.PluralType.CARDINAL ? "locales" : "locales_ordinals");
    Object[][] map = (Object[][]) bundle.getObject(key);
    for (int i = 0; i < map.length; ++i) {
      if (map[i][0].equals(languageCode)) {
        return (String) map[i][1];
      }
    }
    return ""; // We don't currently need to distinguish between "not found" and "use default".
  }

  private PluralRules makePluralRules(Object[][] ruleData) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ruleData.length; ++i) {
      if (i > 0) {
        sb.append("; ");
      }
      sb.append(ruleData[i][0]);
      sb.append(':');
      sb.append(ruleData[i][1]);
    }

    try {
      return PluralRules.parseDescription(sb.toString());
    } catch (ParseException ex) {
      throw new AssertionError(ex);
    }
  }
}
