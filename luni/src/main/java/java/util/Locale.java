/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import libcore.icu.ICU;

/**
 * {@code Locale} represents a language/country/variant combination. Locales are used to
 * alter the presentation of information such as numbers or dates to suit the conventions
 * in the region they describe.
 *
 * <p>The language codes are two-letter lowercase ISO language codes (such as "en") as defined by
 * <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1</a>.
 * The country codes are two-letter uppercase ISO country codes (such as "US") as defined by
 * <a href="http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3">ISO 3166-1</a>.
 * The variant codes are unspecified.
 *
 * <p>Note that Java uses several deprecated two-letter codes. The Hebrew ("he") language
 * code is rewritten as "iw", Indonesian ("id") as "in", and Yiddish ("yi") as "ji". This
 * rewriting happens even if you construct your own {@code Locale} object, not just for
 * instances returned by the various lookup methods.
 *
 * <a name="available_locales"><h3>Available locales</h3></a>
 * <p>This class' constructors do no error checking. You can create a {@code Locale} for languages
 * and countries that don't exist, and you can create instances for combinations that don't
 * exist (such as "de_US" for "German as spoken in the US").
 *
 * <p>Note that locale data is not necessarily available for any of the locales pre-defined as
 * constants in this class except for en_US, which is the only locale Java guarantees is always
 * available.
 *
 * <p>It is also a mistake to assume that all devices have the same locales available.
 * A device sold in the US will almost certainly support en_US and es_US, but not necessarily
 * any locales with the same language but different countries (such as en_GB or es_ES),
 * nor any locales for other languages (such as de_DE). The opposite may well be true for a device
 * sold in Europe.
 *
 * <p>You can use {@link Locale#getDefault} to get an appropriate locale for the <i>user</i> of the
 * device you're running on, or {@link Locale#getAvailableLocales} to get a list of all the locales
 * available on the device you're running on.
 *
 * <a name="locale_data"><h3>Locale data</h3></a>
 * <p>Note that locale data comes solely from ICU. User-supplied locale service providers (using
 * the {@code java.text.spi} or {@code java.util.spi} mechanisms) are not supported.
 *
 * <p>Here are the versions of ICU (and the corresponding CLDR and Unicode versions) used in
 * various Android releases:
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <tr><td>Android 1.5 (Cupcake)/Android 1.6 (Donut)/Android 2.0 (Eclair)</td>
 *     <td>ICU 3.8</td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-5">CLDR 1.5</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode5.0.0/">Unicode 5.0</a></td></tr>
 * <tr><td>Android 2.2 (Froyo)</td>
 *     <td>ICU 4.2</td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-7">CLDR 1.7</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode5.1.0/">Unicode 5.1</a></td></tr>
 * <tr><td>Android 2.3 (Gingerbread)/Android 3.0 (Honeycomb)</td>
 *     <td>ICU 4.4</td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-8">CLDR 1.8</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode5.2.0/">Unicode 5.2</a></td></tr>
 * <tr><td>Android 4.0 (Ice Cream Sandwich)</td>
 *     <td>ICU 4.6</td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-1-9">CLDR 1.9</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode6.0.0/">Unicode 6.0</a></td></tr>
 * <tr><td>Android 4.1 (Jelly Bean)</td>
 *     <td>ICU 4.8</td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-2-0">CLDR 2.0</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode6.0.0/">Unicode 6.0</a></td></tr>
 * <tr><td>Android 4.3 (Jelly Bean MR2)</td>
 *     <td>ICU 50</td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-22-1">CLDR 22.1</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode6.2.0/">Unicode 6.2</a></td></tr>
 * <tr><td>Android 4.4 (KitKat)</td>
 *     <td>ICU 51</td>
 *     <td><a href="http://cldr.unicode.org/index/downloads/cldr-23">CLDR 23</a></td>
 *     <td><a href="http://www.unicode.org/versions/Unicode6.2.0/">Unicode 6.2</a></td></tr>
 * </table>
 *
 * <a name="default_locale"><h3>Be wary of the default locale</h3></a>
 * <p>Note that there are many convenience methods that automatically use the default locale, but
 * using them may lead to subtle bugs.
 *
 * <p>The default locale is appropriate for tasks that involve presenting data to the user. In
 * this case, you want to use the user's date/time formats, number
 * formats, rules for conversion to lowercase, and so on. In this case, it's safe to use the
 * convenience methods.
 *
 * <p>The default locale is <i>not</i> appropriate for machine-readable output. The best choice
 * there is usually {@code Locale.US}&nbsp;&ndash; this locale is guaranteed to be available on all
 * devices, and the fact that it has no surprising special cases and is frequently used (especially
 * for computer-computer communication) means that it tends to be the most efficient choice too.
 *
 * <p>A common mistake is to implicitly use the default locale when producing output meant to be
 * machine-readable. This tends to work on the developer's test devices (especially because so many
 * developers use en_US), but fails when run on a device whose user is in a more complex locale.
 *
 * <p>For example, if you're formatting integers some locales will use non-ASCII decimal
 * digits. As another example, if you're formatting floating-point numbers some locales will use
 * {@code ','} as the decimal point and {@code '.'} for digit grouping. That's correct for
 * human-readable output, but likely to cause problems if presented to another
 * computer ({@link Double#parseDouble} can't parse such a number, for example).
 * You should also be wary of the {@link String#toLowerCase} and
 * {@link String#toUpperCase} overloads that don't take a {@code Locale}: in Turkey, for example,
 * the characters {@code 'i'} and {@code 'I'} won't be converted to {@code 'I'} and {@code 'i'}.
 * This is the correct behavior for Turkish text (such as user input), but inappropriate for, say,
 * HTTP headers.
 */
public final class Locale implements Cloneable, Serializable {

    private static final long serialVersionUID = 9149081749638150636L;

    /**
     * Locale constant for en_CA.
     */
    public static final Locale CANADA = new Locale(true, "en", "CA");

    /**
     * Locale constant for fr_CA.
     */
    public static final Locale CANADA_FRENCH = new Locale(true, "fr", "CA");

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale CHINA = new Locale(true, "zh", "CN");

    /**
     * Locale constant for zh.
     */
    public static final Locale CHINESE = new Locale(true, "zh", "");

    /**
     * Locale constant for en.
     */
    public static final Locale ENGLISH = new Locale(true, "en", "");

    /**
     * Locale constant for fr_FR.
     */
    public static final Locale FRANCE = new Locale(true, "fr", "FR");

    /**
     * Locale constant for fr.
     */
    public static final Locale FRENCH = new Locale(true, "fr", "");

    /**
     * Locale constant for de.
     */
    public static final Locale GERMAN = new Locale(true, "de", "");

    /**
     * Locale constant for de_DE.
     */
    public static final Locale GERMANY = new Locale(true, "de", "DE");

    /**
     * Locale constant for it.
     */
    public static final Locale ITALIAN = new Locale(true, "it", "");

    /**
     * Locale constant for it_IT.
     */
    public static final Locale ITALY = new Locale(true, "it", "IT");

    /**
     * Locale constant for ja_JP.
     */
    public static final Locale JAPAN = new Locale(true, "ja", "JP");

    /**
     * Locale constant for ja.
     */
    public static final Locale JAPANESE = new Locale(true, "ja", "");

    /**
     * Locale constant for ko_KR.
     */
    public static final Locale KOREA = new Locale(true, "ko", "KR");

    /**
     * Locale constant for ko.
     */
    public static final Locale KOREAN = new Locale(true, "ko", "");

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale PRC = new Locale(true, "zh", "CN");

    /**
     * Locale constant for the root locale. The root locale has an empty language,
     * country, and variant.
     *
     * @since 1.6
     */
    public static final Locale ROOT = new Locale(true, "", "");

    /**
     * Locale constant for zh_CN.
     */
    public static final Locale SIMPLIFIED_CHINESE = new Locale(true, "zh", "CN");

    /**
     * Locale constant for zh_TW.
     */
    public static final Locale TAIWAN = new Locale(true, "zh", "TW");

    /**
     * Locale constant for zh_TW.
     */
    public static final Locale TRADITIONAL_CHINESE = new Locale(true, "zh", "TW");

    /**
     * Locale constant for en_GB.
     */
    public static final Locale UK = new Locale(true, "en", "GB");

    /**
     * Locale constant for en_US.
     */
    public static final Locale US = new Locale(true, "en", "US");

    public static final char PRIVATE_USE_EXTENSION = 'x';

    public static final char UNICODE_LOCALE_EXTENSION = 'u';

    /**
     * The current default locale. It is temporarily assigned to US because we
     * need a default locale to lookup the real default locale.
     */
    private static Locale defaultLocale = US;

    static {
        String language = System.getProperty("user.language", "en");
        String region = System.getProperty("user.region", "US");
        String variant = System.getProperty("user.variant", "");
        defaultLocale = new Locale(language, region, variant);
    }

    /**
     * @hide
     */
    public static final class Builder {
        private String language;
        private String region;
        private String variant;
        private String script;

        private final Set<String> attributes;
        private final Map<String, String> keywords;
        private final Map<Character, String> extensions;

        public Builder() {
            attributes = new TreeSet<String>();
            keywords = new TreeMap<String, String>();
            extensions = new TreeMap<Character, String>();
        }

        public Builder setLanguage(String language) {
            if (language == null || language.isEmpty()) {
                this.language = null;
                return this;
            }

            final String lowercaseLanguage = language.toLowerCase(Locale.ROOT);
            if (!isAsciiAlphaWithBoundedLength(lowercaseLanguage, 2, 8)) {
                throw new IllformedLocaleException("Invalid language: " + language);
            }

            this.language = language;
            return this;
        }

        public Builder setLanguageTag(String languageTag) {
            if (languageTag == null || languageTag.isEmpty()) {
                clear();
                return this;
            }

            final Locale fromIcu = ICU.forLanguageTag(languageTag, true /* strict */);
            if (fromIcu == null) {
                throw new IllformedLocaleException("Invalid languageTag: " + languageTag);
            }

            setLocale(fromIcu);
            return this;
        }

        public Builder setRegion(String region) {
            if (region == null || region.isEmpty()) {
                this.region = null;
                return this;
            }

            final String uppercaseRegion = region.toUpperCase(Locale.ROOT);
            if (!isAsciiAlphaWithBoundedLength(uppercaseRegion, 2, 2) &&
                    !isUnM49AreaCode(uppercaseRegion)) {
                throw new IllformedLocaleException("Invalid region: " + region);
            }

            this.region = region;
            return this;
        }

        public Builder setVariant(String variant) {
            if (variant == null || variant.isEmpty()) {
                this.variant = null;
                return this;
            }

            // Note that unlike extensions, we canonicalize to lower case alphabets
            // and underscores instead of hyphens.
            final String lowerCaseVariant = variant.toLowerCase(Locale.ROOT).replace('-', '_');
            String[] subTags = lowerCaseVariant.split("_");

            // The BCP-47 spec states that :
            // - Each variant subtag that starts with a letter must be [5, 8] chars in
            //   length.
            // - Each variant subtag that starts with a number must be exactly 4
            //   chars in length.
            for (String subTag : subTags) {
                if (subTag.length() >= 5 && subTag.length() <= 8) {
                    final char firstChar = subTag.charAt(0);

                    // Also check that the first character of this subtag isn't a number.
                    // Such tags must be exactly 4 chars in length.
                    if (!isAsciiAlphaNum(subTag) || (firstChar >= '0' && firstChar <= '9')) {
                        throw new IllformedLocaleException("Invalid variant: " + variant);
                    }
                } else if (subTag.length() == 4) {
                    final char firstChar = subTag.charAt(0);
                    if (!isAsciiAlphaNum(subTag) || !(firstChar >= '0' && firstChar <= '9')) {
                        throw new IllformedLocaleException("Invalid variant: " + variant);
                    }
                } else {
                    throw new IllformedLocaleException("Invalid variant: " + variant);
                }
            }


            this.variant = lowerCaseVariant;
            return this;
        }

        public Builder setScript(String script) {
            if (script == null || script.isEmpty()) {
                this.script = null;
                return this;
            }

            final String lowercaseScript = script.toLowerCase(Locale.ROOT);
            if (!isAsciiAlphaWithBoundedLength(lowercaseScript, 4, 4)) {
                throw new IllformedLocaleException("Invalid script: " + script);
            }

            this.script = titleCaseAscii(lowercaseScript);
            return this;
        }

        public Builder setLocale(Locale locale) {
            final String backupLanguage = language;
            final String backupRegion = region;
            final String backupVariant = variant;

            try {
                setLanguage(locale.getLanguage());
                setRegion(locale.getCountry());
                setVariant(locale.getVariant());
            } catch (IllformedLocaleException ifle) {
                language = backupLanguage;
                region = backupRegion;
                variant = backupVariant;

                throw ifle;
            }

            // These values can be set only via the builder class, so there's
            // no need to normalize them or check their validity.
            this.script = locale.getScript();

            extensions.clear();
            extensions.putAll(locale.bcp47Extensions);

            keywords.clear();
            keywords.putAll(locale.unicodeKeywords);

            attributes.clear();
            attributes.addAll(locale.unicodeAttributes);

            return this;
        }

        public Builder addUnicodeLocaleAttribute(String attribute) {
            if (attribute == null) {
                throw new NullPointerException("attribute == null");
            }

            final String lowercaseAttribute = attribute.toLowerCase(Locale.ROOT);
            if (!isAsciiAlphaNumWithBoundedLength(lowercaseAttribute, 3, 8)) {
                throw new IllformedLocaleException("Invalid locale attribute: " + attribute);
            }

            attributes.add(attribute);

            return this;
        }

        public Builder removeUnicodeLocaleAttribute(String attribute) {
            if (attribute == null) {
                throw new NullPointerException("attribute == null");
            }

            // Weirdly, remove is specified to check whether the attribute
            // is valid, so we have to perform the full alphanumeric check here.
            final String lowercaseAttribute = attribute.toLowerCase(Locale.ROOT);
            if (!isAsciiAlphaNumWithBoundedLength(lowercaseAttribute, 3, 8)) {
                throw new IllformedLocaleException("Invalid locale attribute: " + attribute);
            }

            attributes.remove(attribute);
            return this;
        }

        public Builder setExtension(char key, String value) {
            final String[] subtags = value.toLowerCase(Locale.ROOT).replace('_', '-').split("-");

            // Lengths for subtags in the private use extension should be [1, 8] chars.
            // For all other extensions, they should be [2, 8] chars.
            //
            // http://www.rfc-editor.org/rfc/bcp/bcp47.txt
            final int minimumLength = (key == PRIVATE_USE_EXTENSION) ? 1 : 2;
            for (String subtag : subtags) {
                if (!isAsciiAlphaNumWithBoundedLength(subtag, minimumLength, 8)) {
                    throw new IllformedLocaleException(
                            "Invalid private use extension : " + value);
                }
            }

            // We need to take special action in the case of unicode extensions,
            // since we claim to understand their keywords and attributes.
            if (key == UNICODE_LOCALE_EXTENSION) {
                // First clear existing attributes and keywords.
                extensions.clear();
                attributes.clear();

                parseUnicodeExtension(subtags, keywords, attributes);
            }

            return this;
        }

        public Builder clearExtensions() {
            extensions.clear();
            attributes.clear();
            keywords.clear();
            return this;
        }

        /*
         * keyword = key [sep type]
         * key = 2alphanum
         * type = 3*8alphanum *(sep 3*8alphanum)
         * alphanum = [0-9 A-Z a-z]
         */
        public Builder setUnicodeLocaleKeyword(String key, String type) {
            if (key == null) {
                throw new NullPointerException("key == null");
            }

            if (type == null && keywords != null) {
                keywords.remove(key);
                return this;
            }

            final String lowerCaseKey = key.toLowerCase(Locale.ROOT);
            // The key must be exactly two alphanumeric characters.
            if (lowerCaseKey.length() != 2 || !isAsciiAlphaNum(lowerCaseKey)) {
                throw new IllformedLocaleException("Invalid unicode locale keyword: " + key);
            }

            // The type can be one or more alphanumeric strings of length [3, 8] characters,
            // separated by a separator char, which is one of "_" or "-". Though the spec
            // doesn't require it, we normalize all "_" to "-" to make the rest of our
            // processing easier.
            final String lowerCaseType = type.toLowerCase(Locale.ROOT).replace("_", "-");
            if (!isValidTypeList(lowerCaseType)) {
                throw new IllformedLocaleException("Invalid unicode locale type: " + type);
            }

            // Everything checks out fine, add the <key, type> mapping to the list.
            keywords.put(key, lowerCaseType);

            return this;
        }

        public Builder clear() {
            clearExtensions();
            language = null;
            region = null;
            variant = null;
            script = null;

            return this;
        }

        public Locale build() {
            addUnicodeExtensionToExtensionsMap();

            // NOTE: We need to make a copy of attributes, keywords and extensions
            // because the RI allows this builder to reused.
            return new Locale(language, region, variant, script,
                    attributes, keywords, extensions,
                    false /* from public constructor */);
        }

        private void addUnicodeExtensionToExtensionsMap() {
            if (attributes.isEmpty() && keywords.isEmpty()) {
                return;
            }

            StringBuilder sb = new StringBuilder(64);
            if (!attributes.isEmpty()) {
                Iterator<String> attributesIterator = attributes.iterator();
                while (true) {
                    sb.append(attributesIterator.next());
                    if (attributesIterator.hasNext()) {
                        sb.append('-');
                    } else {
                        break;
                    }
                }
            }

            if (!keywords.isEmpty()) {
                if (!attributes.isEmpty()) {
                    sb.append('-');
                }

                Iterator<Map.Entry<String, String>> keywordsIterator = keywords.entrySet().iterator();
                while (true) {
                    final Map.Entry<String, String> keyWord = keywordsIterator.next();
                    sb.append(keyWord.getKey());
                    sb.append('-');
                    sb.append(keyWord.getValue());
                    if (keywordsIterator.hasNext()) {
                        sb.append('-');
                    } else {
                        break;
                    }
                }
            }

            extensions.put(UNICODE_LOCALE_EXTENSION, sb.toString());
        }
    }

    public static Locale forLanguageTag(String languageTag) {
        if (languageTag == null) {
            throw new NullPointerException("languageTag == null");
        }

        return ICU.forLanguageTag(languageTag, false /* strict */);
    }

    private transient String countryCode;
    private transient String languageCode;
    private transient String variantCode;
    private transient String scriptCode;
    private transient String cachedToStringResult;
    private transient String cachedLanguageTag;

    /* Sorted, UnmodifiableSet */ private Set<String> unicodeAttributes;
    /* Sorted, UnmodifiableMap */ private Map<String, String> unicodeKeywords;
    /* Sorted, UnmodifiableMap */ private Map<Character, String> bcp47Extensions;

    /**
     * There's a circular dependency between toLowerCase/toUpperCase and
     * Locale.US. Work around this by avoiding these methods when constructing
     * the built-in locales.
     *
     * @param unused required for this constructor to have a unique signature
     */
    private Locale(boolean unused, String lowerCaseLanguageCode, String upperCaseCountryCode) {
        this.languageCode = lowerCaseLanguageCode;
        this.countryCode = upperCaseCountryCode;
        this.variantCode = "";
        this.scriptCode = "";

        this.unicodeAttributes = Collections.EMPTY_SET;
        this.unicodeKeywords = Collections.EMPTY_MAP;
        this.bcp47Extensions = Collections.EMPTY_MAP;
    }

    /**
     * Constructs a new {@code Locale} using the specified language.
     */
    public Locale(String language) {
        this(language, "", "", "", Collections.EMPTY_SET, Collections.EMPTY_MAP,
                Collections.EMPTY_MAP, true /* from public constructor */);
    }

    /**
     * Constructs a new {@code Locale} using the specified language and country codes.
     */
    public Locale(String language, String country) {
        this(language, country, "", "", Collections.EMPTY_SET, Collections.EMPTY_MAP,
                Collections.EMPTY_MAP, true /* from public constructor */);
    }

    /** @hide */
    public Locale(String language, String country, String variant, String scriptCode,
            /* nonnull */ Set<String> unicodeAttributes,
            /* nonnull */ Map<String, String> unicodeKeywords,
            /* nonnull */ Map<Character, String> bcp47Extensions,
            boolean fromPublicConstructor) {
        if (language == null || country == null || variant == null) {
            throw new NullPointerException("language=" + language +
                    ",country=" + country +
                    ",variant=" + variant);
        }

        if (fromPublicConstructor) {
            if (language.isEmpty() && country.isEmpty()) {
                languageCode = "";
                countryCode = "";
                variantCode = variant;
            } else {
                languageCode = language.toLowerCase(Locale.US);
                // Map new language codes to the obsolete language
                // codes so the correct resource bundles will be used.
                if (languageCode.equals("he")) {
                    languageCode = "iw";
                } else if (languageCode.equals("id")) {
                    languageCode = "in";
                } else if (languageCode.equals("yi")) {
                    languageCode = "ji";
                }

                countryCode = country.toUpperCase(Locale.US);

                // Work around for be compatible with RI
                variantCode = variant;
            }
        } else {
            this.languageCode = language;
            this.countryCode = country;
            this.variantCode = variant;
        }

        this.scriptCode = scriptCode;

        if (fromPublicConstructor) {
            this.unicodeAttributes = unicodeAttributes;
            this.unicodeKeywords = unicodeKeywords;
            this.bcp47Extensions = bcp47Extensions;
        } else {
            this.unicodeAttributes = Collections.unmodifiableSet(
                    new HashSet<String>(unicodeAttributes));
            this.unicodeKeywords = Collections.unmodifiableMap(
                    new HashMap<String, String>(unicodeKeywords));
            this.bcp47Extensions = Collections.unmodifiableMap(
                    new HashMap<Character, String>(bcp47Extensions));
        }
    }

    /**
     * Constructs a new {@code Locale} using the specified language, country,
     * and variant codes.
     */
    public Locale(String language, String country, String variant) {
        this(language, country, variant, "", Collections.EMPTY_SET,
                Collections.EMPTY_MAP, Collections.EMPTY_MAP,
                true /* from public constructor */);
    }

    @Override public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns true if {@code object} is a locale with the same language,
     * country and variant.
     */
    @Override public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Locale) {
            Locale o = (Locale) object;
            return languageCode.equals(o.languageCode)
                    && countryCode.equals(o.countryCode)
                    && variantCode.equals(o.variantCode);
        }
        return false;
    }

    /**
     * Returns the system's installed locales. This array always includes {@code
     * Locale.US}, and usually several others. Most locale-sensitive classes
     * offer their own {@code getAvailableLocales} method, which should be
     * preferred over this general purpose method.
     *
     * @see java.text.BreakIterator#getAvailableLocales()
     * @see java.text.Collator#getAvailableLocales()
     * @see java.text.DateFormat#getAvailableLocales()
     * @see java.text.DateFormatSymbols#getAvailableLocales()
     * @see java.text.DecimalFormatSymbols#getAvailableLocales()
     * @see java.text.NumberFormat#getAvailableLocales()
     * @see java.util.Calendar#getAvailableLocales()
     */
    public static Locale[] getAvailableLocales() {
        return ICU.getAvailableLocales();
    }

    /**
     * Returns the country code for this locale, or {@code ""} if this locale
     * doesn't correspond to a specific country.
     */
    public String getCountry() {
        return countryCode;
    }

    /**
     * Returns the user's preferred locale. This may have been overridden for
     * this process with {@link #setDefault}.
     *
     * <p>Since the user's locale changes dynamically, avoid caching this value.
     * Instead, use this method to look it up for each use.
     */
    public static Locale getDefault() {
        return defaultLocale;
    }

    /**
     * Equivalent to {@code getDisplayCountry(Locale.getDefault())}.
     */
    public final String getDisplayCountry() {
        return getDisplayCountry(getDefault());
    }

    /**
     * Returns the name of this locale's country, localized to {@code locale}.
     * Returns the empty string if this locale does not correspond to a specific
     * country.
     */
    public String getDisplayCountry(Locale locale) {
        if (countryCode.isEmpty()) {
            return "";
        }
        String result = ICU.getDisplayCountryNative(toString(), locale.toString());
        if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
            result = ICU.getDisplayCountryNative(toString(), Locale.getDefault().toString());
        }
        return result;
    }

    /**
     * Equivalent to {@code getDisplayLanguage(Locale.getDefault())}.
     */
    public final String getDisplayLanguage() {
        return getDisplayLanguage(getDefault());
    }

    /**
     * Returns the name of this locale's language, localized to {@code locale}.
     * If the language name is unknown, the language code is returned.
     */
    public String getDisplayLanguage(Locale locale) {
        if (languageCode.isEmpty()) {
            return "";
        }

        // http://b/8049507 --- frameworks/base should use fil_PH instead of tl_PH.
        // Until then, we're stuck covering their tracks, making it look like they're
        // using "fil" when they're not.
        String localeString = toString();
        if (languageCode.equals("tl")) {
            localeString = toNewString("fil", countryCode, variantCode);
        }

        String result = ICU.getDisplayLanguageNative(localeString, locale.toString());
        if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
            result = ICU.getDisplayLanguageNative(localeString, Locale.getDefault().toString());
        }
        return result;
    }

    /**
     * Equivalent to {@code getDisplayName(Locale.getDefault())}.
     */
    public final String getDisplayName() {
        return getDisplayName(getDefault());
    }

    /**
     * Returns this locale's language name, country name, and variant, localized
     * to {@code locale}. The exact output form depends on whether this locale
     * corresponds to a specific language, country and variant.
     *
     * <p>For example:
     * <ul>
     * <li>{@code new Locale("en").getDisplayName(Locale.US)} -> {@code English}
     * <li>{@code new Locale("en", "US").getDisplayName(Locale.US)} -> {@code English (United States)}
     * <li>{@code new Locale("en", "US", "POSIX").getDisplayName(Locale.US)} -> {@code English (United States,Computer)}
     * <li>{@code new Locale("en").getDisplayName(Locale.FRANCE)} -> {@code anglais}
     * <li>{@code new Locale("en", "US").getDisplayName(Locale.FRANCE)} -> {@code anglais (États-Unis)}
     * <li>{@code new Locale("en", "US", "POSIX").getDisplayName(Locale.FRANCE)} -> {@code anglais (États-Unis,informatique)}.
     * </ul>
     */
    public String getDisplayName(Locale locale) {
        int count = 0;
        StringBuilder buffer = new StringBuilder();
        if (!languageCode.isEmpty()) {
            String displayLanguage = getDisplayLanguage(locale);
            buffer.append(displayLanguage.isEmpty() ? languageCode : displayLanguage);
            ++count;
        }
        if (!countryCode.isEmpty()) {
            if (count == 1) {
                buffer.append(" (");
            }
            String displayCountry = getDisplayCountry(locale);
            buffer.append(displayCountry.isEmpty() ? countryCode : displayCountry);
            ++count;
        }
        if (!variantCode.isEmpty()) {
            if (count == 1) {
                buffer.append(" (");
            } else if (count == 2) {
                buffer.append(",");
            }
            String displayVariant = getDisplayVariant(locale);
            buffer.append(displayVariant.isEmpty() ? variantCode : displayVariant);
            ++count;
        }
        if (count > 1) {
            buffer.append(")");
        }
        return buffer.toString();
    }

    /**
     * Returns the full variant name in the default {@code Locale} for the variant code of
     * this {@code Locale}. If there is no matching variant name, the variant code is
     * returned.
     */
    public final String getDisplayVariant() {
        return getDisplayVariant(getDefault());
    }

    /**
     * Returns the full variant name in the specified {@code Locale} for the variant code
     * of this {@code Locale}. If there is no matching variant name, the variant code is
     * returned.
     */
    public String getDisplayVariant(Locale locale) {
        if (variantCode.length() == 0) {
            return variantCode;
        }
        String result = ICU.getDisplayVariantNative(toString(), locale.toString());
        if (result == null) { // TODO: do we need to do this, or does ICU do it for us?
            result = ICU.getDisplayVariantNative(toString(), Locale.getDefault().toString());
        }
        return result;
    }

    /**
     * Returns the three-letter ISO 3166 country code which corresponds to the country
     * code for this {@code Locale}.
     * @throws MissingResourceException if there's no 3-letter country code for this locale.
     */
    public String getISO3Country() {
        String code = ICU.getISO3CountryNative(toString());
        if (!countryCode.isEmpty() && code.isEmpty()) {
            throw new MissingResourceException("No 3-letter country code for locale: " + this, "FormatData_" + this, "ShortCountry");
        }
        return code;
    }

    /**
     * Returns the three-letter ISO 639-2/T language code which corresponds to the language
     * code for this {@code Locale}.
     * @throws MissingResourceException if there's no 3-letter language code for this locale.
     */
    public String getISO3Language() {
        String code = ICU.getISO3LanguageNative(toString());
        if (!languageCode.isEmpty() && code.isEmpty()) {
            throw new MissingResourceException("No 3-letter language code for locale: " + this, "FormatData_" + this, "ShortLanguage");
        }
        return code;
    }

    /**
     * Returns an array of strings containing all the two-letter ISO 3166 country codes that can be
     * used as the country code when constructing a {@code Locale}.
     */
    public static String[] getISOCountries() {
        return ICU.getISOCountries();
    }

    /**
     * Returns an array of strings containing all the two-letter ISO 639-1 language codes that can be
     * used as the language code when constructing a {@code Locale}.
     */
    public static String[] getISOLanguages() {
        return ICU.getISOLanguages();
    }

    /**
     * Returns the language code for this {@code Locale} or the empty string if no language
     * was set.
     */
    public String getLanguage() {
        return languageCode;
    }

    /**
     * Returns the variant code for this {@code Locale} or an empty {@code String} if no variant
     * was set.
     */
    public String getVariant() {
        return variantCode;
    }

    /**
     * @hide
     */
    public String getScript() {
        return scriptCode;
    }

    /**
     * @hide
     */
    public String getDisplayScript() {
        if (scriptCode.isEmpty()) {
            return "";
        }

        final String scriptName = ICU.getScriptName(scriptCode);
        if (scriptName == null) {
            return "";
        }

        return scriptName;
    }

    /**
     * @hide
     */
    public String getDisplayScript(Locale locale) {
        return getDisplayScript();
    }

    /**
     * @hide
     */
    public String toLanguageTag() {
        if (cachedLanguageTag == null) {
            cachedLanguageTag = ICU.toLanguageTag(this);
        }

        return cachedLanguageTag;
    }

    /**
     * @hide
     */
    public Set<Character> getExtensionKeys() {
        return bcp47Extensions.keySet();
    }

    /**
     * @hide
     */
    public String getExtension(char extensionKey) {
        return bcp47Extensions.get(extensionKey);
    }

    /**
     * @hide
     */
    public String getUnicodeLocaleType(String key) {
        return unicodeKeywords.get(key);
    }

    /**
     * @hide
     */
	public Set<String> getUnicodeLocaleAttributes() {
        return unicodeAttributes;
    }

    /**
     * @hide
     */
	public Set<String> getUnicodeLocaleKeys() {
        return unicodeKeywords.keySet();
    }

    @Override
    public synchronized int hashCode() {
        return countryCode.hashCode() + languageCode.hashCode()
                + variantCode.hashCode();
    }

    /**
     * Overrides the default locale. This does not affect system configuration,
     * and attempts to override the system-provided default locale may
     * themselves be overridden by actual changes to the system configuration.
     * Code that calls this method is usually incorrect, and should be fixed by
     * passing the appropriate locale to each locale-sensitive method that's
     * called.
     */
    public synchronized static void setDefault(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }
        defaultLocale = locale;
    }

    /**
     * Returns the string representation of this {@code Locale}. It consists of the
     * language code, country code and variant separated by underscores.
     * If the language is missing the string begins
     * with an underscore. If the country is missing there are 2 underscores
     * between the language and the variant. The variant cannot stand alone
     * without a language and/or country code: in this case this method would
     * return the empty string.
     *
     * <p>Examples: "en", "en_US", "_US", "en__POSIX", "en_US_POSIX"
     */
    @Override
    public final String toString() {
        String result = cachedToStringResult;
        if (result == null) {
            result = cachedToStringResult = toNewString();
        }
        return result;
    }

    private String toNewString() {
        // The string form of a locale that only has a variant is the empty string.
        if (languageCode.length() == 0 && countryCode.length() == 0) {
            return "";
        }
        // Otherwise, the output format is "ll_cc_variant", where language and country are always
        // two letters, but the variant is an arbitrary length. A size of 11 characters has room
        // for "en_US_POSIX", the largest "common" value. (In practice, the string form is almost
        // always 5 characters: "ll_cc".)
        StringBuilder result = new StringBuilder(11);
        result.append(languageCode);
        if (countryCode.length() > 0 || variantCode.length() > 0) {
            result.append('_');
        }
        result.append(countryCode);
        if (variantCode.length() > 0) {
            result.append('_');
        }
        result.append(variantCode);

        if (!scriptCode.isEmpty()) {
            result.append('#');
            result.append(scriptCode);
        }

        if (!bcp47Extensions.isEmpty()) {
            result.append('-');
            result.append(serializeExtensions(bcp47Extensions));
        }

        return result.toString();
    }

    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("country", String.class),
        new ObjectStreamField("hashcode", int.class),
        new ObjectStreamField("language", String.class),
        new ObjectStreamField("variant", String.class),
        new ObjectStreamField("script", String.class),
        new ObjectStreamField("extensions", String.class),
    };

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("country", countryCode);
        fields.put("hashcode", -1);
        fields.put("language", languageCode);
        fields.put("variant", variantCode);
        fields.put("script", scriptCode);

        if (!bcp47Extensions.isEmpty()) {
            fields.put("extensions", serializeExtensions(bcp47Extensions));
        }

        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        countryCode = (String) fields.get("country", "");
        languageCode = (String) fields.get("language", "");
        variantCode = (String) fields.get("variant", "");
        scriptCode = (String) fields.get("script", "");

        this.unicodeKeywords = Collections.EMPTY_MAP;
        this.unicodeAttributes = Collections.EMPTY_SET;
        this.bcp47Extensions = Collections.EMPTY_MAP;

        String extensions = (String) fields.get("extensions", null);
        if (extensions != null) {
            Map<Character, String> extensionsMap = new TreeMap<Character, String>();
            parseSerializedExtensions(extensions, extensionsMap);
            this.bcp47Extensions = Collections.unmodifiableMap(extensionsMap);

            if (extensionsMap.containsKey(UNICODE_LOCALE_EXTENSION)) {
                String unicodeExtension = extensionsMap.get(UNICODE_LOCALE_EXTENSION);
                String[] subTags = unicodeExtension.split("-");

                Map<String, String> unicodeKeywords = new TreeMap<String, String>();
                Set<String> unicodeAttributes = new TreeSet<String>();
                parseUnicodeExtension(subTags, unicodeKeywords, unicodeAttributes);

                this.unicodeKeywords = Collections.unmodifiableMap(unicodeKeywords);
                this.unicodeAttributes = Collections.unmodifiableSet(unicodeAttributes);
            }
        }
    }

    static String serializeExtensions(Map<Character, String> extensionsMap) {
        Iterator<Map.Entry<Character, String>> entryIterator = extensionsMap.entrySet().iterator();
        StringBuilder sb = new StringBuilder(64);

        while (true) {
            final Map.Entry<Character, String> entry = entryIterator.next();
            sb.append(entry.getKey());
            sb.append('-');
            sb.append(entry.getValue());

            if (entryIterator.hasNext()) {
                sb.append('-');
            } else {
                break;
            }
        }

        return sb.toString();
    }

    // Visible for testing
    static void parseSerializedExtensions(String extString, Map<Character, String> outputMap) {
        final String[] subTags = extString.split("-");
        int[] extensionStartIndices = new int[subTags.length / 2];

        int length = 0;
        int count = 0;
        for (String subTag : subTags) {
            if (subTag.length() == 1) {
                extensionStartIndices[count++] = length;
            }

            if (subTag.length() > 0) {
                length += subTag.length();
            }
        }

        for (int i = 0; i < count; ++i) {
            final int valueStart = extensionStartIndices[i] + 2;
            final int valueEnd = (i == count) ?
                    extString.length() : extensionStartIndices[i + 1];

            outputMap.put(extString.charAt(extensionStartIndices[i]),
                    extString.substring(valueStart, valueEnd));
        }
    }


    private static boolean isUnM49AreaCode(String code) {
        if (code.length() != 3) {
            return false;
        }

        for (int i = 0; i < 3; ++i) {
            final char character = code.charAt(i);
            if (!(character >= '0' && character <= '9')) {
                return false;
            }
        }

        return true;
    }

    /*
     * Checks whether a given string is an ASCII alphanumeric string.
     */
    private static boolean isAsciiAlphaNum(String string) {
        for (int i = 0; i < string.length(); i++) {
            final char character = string.charAt(i);
            if (!(character >= 'a' && character <= 'z' ||
                    character >= 'A' && character <= 'Z' ||
                    character >= '0' && character <= '9')) {
                return false;
            }
        }

        return true;
    }

    private static boolean isAsciiAlphaWithBoundedLength(String string,
            int lowerBound, int upperBound) {
        final int length = string.length();
        if (length < lowerBound || length > upperBound) {
            return false;
        }

        for (int i = 0; i < length; ++i) {
            final char character = string.charAt(i);
            if (!(character >= 'a' && character <= 'z' ||
                    character >= 'A' && character <= 'Z')) {
                return false;
            }
        }

        return true;
    }

    private static boolean isAsciiAlphaNumWithBoundedLength(String attributeOrType,
            int lowerBound, int upperBound) {
        if (attributeOrType.length() < lowerBound || attributeOrType.length() > upperBound) {
            return false;
        }

        return isAsciiAlphaNum(attributeOrType);
    }

    private static String titleCaseAscii(String lowerCase) {
        try {
            byte[] chars = lowerCase.getBytes(StandardCharsets.US_ASCII);
            chars[0] = (byte) ((int) chars[0] + 'A' - 'a');
            return new String(chars, StandardCharsets.US_ASCII);
        } catch (UnsupportedOperationException uoe) {
            throw new AssertionError();
        }
    }

    private static boolean isValidTypeList(String lowerCaseTypeList) {
        final String[] splitList = lowerCaseTypeList.split("-");
        for (String type : splitList) {
            if (!isAsciiAlphaNumWithBoundedLength(type, 3, 8)) {
                return false;
            }
        }

        return true;
    }

    // Visible for testing.
    static void parseUnicodeExtension(String[] subtags,
            Map<String, String> keywords, Set<String> attributes)  {
        // This extension is described by http://www.unicode.org/reports/tr35/#RFC5234
        //
        // unicode_locale_extensions = sep "u" (1*(sep keyword) / 1*(sep attribute) *(sep keyword)).
        //
        // In particular, attributes (if any) must appear before keywords.
        String lastKeyword = null;
        List<String> subtagsForKeyword = new ArrayList<String>();
        for (String subtag : subtags) {
            if (subtag.length() == 2) {
                if (subtagsForKeyword.size() > 0) {
                    keywords.put(lastKeyword, joinStrings(subtagsForKeyword));
                    subtagsForKeyword.clear();
                }

                lastKeyword = subtag;
            } else if (subtag.length() > 2) {
                if (lastKeyword == null) {
                    attributes.add(subtag);
                } else {
                    subtagsForKeyword.add(subtag);
                }
            }
        }
    }

    private static String joinStrings(List<String> strings) {
        final int size = strings.size();

        StringBuilder sb = new StringBuilder(strings.get(0).length());
        for (int i = 0; i < size; ++i) {
            sb.append(strings.get(0));
            if (i != size - 1) {
                sb.append('-');
            }
        }

        return sb.toString();
    }
}
