/*
**********************************************************************
* Copyright (c) 2004-2012, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Alan Liu
* Created: April 6, 2004
* Since: ICU 3.0
**********************************************************************
*/

package com.android.i18n;

import java.io.IOException;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.android.i18n.MessagePattern.ArgType;
import com.android.i18n.MessagePattern.Part;
import com.android.i18n.PluralRules.PluralType;

/**
 * <p>MessageFormat prepares strings for display to users,
 * with optional arguments (variables/placeholders).
 * The arguments can occur in any order, which is necessary for translation
 * into languages with different grammars.
 *
 * <p>A MessageFormat is constructed from a <em>pattern</em> string
 * with arguments in {curly braces} which will be replaced by formatted values.
 *
 * <p><code>MessageFormat</code> differs from the other <code>Format</code>
 * classes in that you create a <code>MessageFormat</code> object using its
 * constructor (not with a <code>getInstance</code> style factory
 * method). Factory methods aren't necessary because <code>MessageFormat</code>
 * itself doesn't implement locale-specific behavior. Any locale-specific
 * behavior is defined by the pattern that you provide and the
 * subformats used for inserted arguments.
 *
 * <p>Arguments can be named (using identifiers) or numbered (using small ASCII-digit integers).
 *
 * <p>An argument might not specify any format type. In this case,
 * a Number value is formatted with a default (for the locale) NumberFormat,
 * a Date value is formatted with a default (for the locale) DateFormat,
 * and for any other value its toString() value is used.
 *
 * <p>An argument might specify a "simple" type for which the specified
 * Format object is created, cached and used.
 *
 * <p>An argument might have a "complex" type with nested MessageFormat sub-patterns.
 * During formatting, one of these sub-messages is selected according to the argument value
 * and recursively formatted.
 *
 * <p>Custom formatting can be achieved by writing
 * a typeless argument in the pattern string
 * and supplying it with a preformatted string value.
 *
 * <p>When formatting, MessageFormat takes a collection of argument values
 * and writes an output string.
 * The argument values may be passed as an array
 * (when the pattern contains only numbered arguments)
 * or as a Map (which works for both named and numbered arguments).
 *
 * <p>Each argument is matched with one of the input values by array index or map key
 * and formatted according to its pattern specification
 * (or using a custom Format object if one was set).
 * A numbered pattern argument is matched with a map key that contains that number
 * as an ASCII-decimal-digit string (without leading zero).
 *
 * <h4><a name="patterns">Patterns and Their Interpretation</a></h4>
 *
 * <code>MessageFormat</code> uses patterns of the following form:
 * <blockquote><pre>
 * message = messageText (argument messageText)*
 * argument = noneArg | simpleArg | complexArg
 * complexArg = choiceArg | pluralArg | selectArg | selectOrdinalArg
 *
 * noneArg = '{' argNameOrNumber '}'
 * simpleArg = '{' argNameOrNumber ',' argType [',' argStyle] '}'
 * choiceArg = '{' argNameOrNumber ',' "choice" ',' choiceStyle '}'
 * pluralArg = '{' argNameOrNumber ',' "plural" ',' pluralStyle '}'
 * selectArg = '{' argNameOrNumber ',' "select" ',' selectStyle '}'
 * selectOrdinalArg = '{' argNameOrNumber ',' "selectOrdinal" ',' pluralStyle '}'
 *
 * choiceStyle: see {@link ChoiceFormat}
 * pluralStyle: see {@link PluralFormat}
 * selectStyle: see {@link SelectFormat}
 *
 * argNameOrNumber = argName | argNumber
 * argName = [^[[:Pattern_Syntax:][:Pattern_White_Space:]]]+
 * argNumber = '0' | ('1'..'9' ('0'..'9')*)
 *
 * argType = "number" | "date" | "time" | "spellout" | "ordinal" | "duration"
 * argStyle = "short" | "medium" | "long" | "full" | "integer" | "currency" | "percent" | argStyleText
 * </pre></blockquote>
 *
 * <ul>
 *   <li>messageText can contain quoted literal strings including syntax characters.
 *       A quoted literal string begins with an ASCII apostrophe and a syntax character
 *       (usually a {curly brace}) and continues until the next single apostrophe.
 *       A double ASCII apostrophe inside or outside of a quoted string represents
 *       one literal apostrophe.
 *   <li>Quotable syntax characters are the {curly braces} in all messageText parts,
 *       plus the '#' sign in a messageText immediately inside a pluralStyle,
 *       and the '|' symbol in a messageText immediately inside a choiceStyle.
 *   <li>See also {@link MessagePattern.ApostropheMode}
 *   <li>In argStyleText, every single ASCII apostrophe begins and ends quoted literal text,
 *       and unquoted {curly braces} must occur in matched pairs.
 * </ul>
 *
 * <p>Recommendation: Use the real apostrophe (single quote) character \u2019 for
 * human-readable text, and use the ASCII apostrophe (\u0027 ' )
 * only in program syntax, like quoting in MessageFormat.
 * See the annotations for U+0027 Apostrophe in The Unicode Standard.
 *
 * <p>The <code>choice</code> argument type is deprecated.
 * Use <code>plural</code> arguments for proper plural selection,
 * and <code>select</code> arguments for simple selection among a fixed set of choices.
 *
 * <p>The <code>argType</code> and <code>argStyle</code> values are used to create
 * a <code>Format</code> instance for the format element. The following
 * table shows how the values map to Format instances. Combinations not
 * shown in the table are illegal. Any <code>argStyleText</code> must
 * be a valid pattern string for the Format subclass used.
 *
 * <p><table border=1>
 *    <tr>
 *       <th>argType
 *       <th>argStyle
 *       <th>resulting Format object
 *    <tr>
 *       <td colspan=2><i>(none)</i>
 *       <td><code>null</code>
 *    <tr>
 *       <td rowspan=5><code>number</code>
 *       <td><i>(none)</i>
 *       <td><code>NumberFormat.getInstance(getLocale())</code>
 *    <tr>
 *       <td><code>integer</code>
 *       <td><code>NumberFormat.getIntegerInstance(getLocale())</code>
 *    <tr>
 *       <td><code>currency</code>
 *       <td><code>NumberFormat.getCurrencyInstance(getLocale())</code>
 *    <tr>
 *       <td><code>percent</code>
 *       <td><code>NumberFormat.getPercentInstance(getLocale())</code>
 *    <tr>
 *       <td><i>argStyleText</i>
 *       <td><code>new DecimalFormat(argStyleText, new DecimalFormatSymbols(getLocale()))</code>
 *    <tr>
 *       <td rowspan=6><code>date</code>
 *       <td><i>(none)</i>
 *       <td><code>DateFormat.getDateInstance(DateFormat.DEFAULT, getLocale())</code>
 *    <tr>
 *       <td><code>short</code>
 *       <td><code>DateFormat.getDateInstance(DateFormat.SHORT, getLocale())</code>
 *    <tr>
 *       <td><code>medium</code>
 *       <td><code>DateFormat.getDateInstance(DateFormat.DEFAULT, getLocale())</code>
 *    <tr>
 *       <td><code>long</code>
 *       <td><code>DateFormat.getDateInstance(DateFormat.LONG, getLocale())</code>
 *    <tr>
 *       <td><code>full</code>
 *       <td><code>DateFormat.getDateInstance(DateFormat.FULL, getLocale())</code>
 *    <tr>
 *       <td><i>argStyleText</i>
 *       <td><code>new SimpleDateFormat(argStyleText, getLocale())
 *    <tr>
 *       <td rowspan=6><code>time</code>
 *       <td><i>(none)</i>
 *       <td><code>DateFormat.getTimeInstance(DateFormat.DEFAULT, getLocale())</code>
 *    <tr>
 *       <td><code>short</code>
 *       <td><code>DateFormat.getTimeInstance(DateFormat.SHORT, getLocale())</code>
 *    <tr>
 *       <td><code>medium</code>
 *       <td><code>DateFormat.getTimeInstance(DateFormat.DEFAULT, getLocale())</code>
 *    <tr>
 *       <td><code>long</code>
 *       <td><code>DateFormat.getTimeInstance(DateFormat.LONG, getLocale())</code>
 *    <tr>
 *       <td><code>full</code>
 *       <td><code>DateFormat.getTimeInstance(DateFormat.FULL, getLocale())</code>
 *    <tr>
 *       <td><i>argStyleText</i>
 *       <td><code>new SimpleDateFormat(argStyleText, getLocale())
 *    <tr>
 *       <td><code>spellout</code>
 *       <td><i>argStyleText (optional)</i>
 *       <td><code>new RuleBasedNumberFormat(getLocale(), RuleBasedNumberFormat.SPELLOUT)
 *           <br/>&nbsp;&nbsp;&nbsp;&nbsp;.setDefaultRuleset(argStyleText);</code>
 *    <tr>
 *       <td><code>ordinal</code>
 *       <td><i>argStyleText (optional)</i>
 *       <td><code>new RuleBasedNumberFormat(getLocale(), RuleBasedNumberFormat.ORDINAL)
 *           <br/>&nbsp;&nbsp;&nbsp;&nbsp;.setDefaultRuleset(argStyleText);</code>
 *    <tr>
 *       <td><code>duration</code>
 *       <td><i>argStyleText (optional)</i>
 *       <td><code>new RuleBasedNumberFormat(getLocale(), RuleBasedNumberFormat.DURATION)
 *           <br/>&nbsp;&nbsp;&nbsp;&nbsp;.setDefaultRuleset(argStyleText);</code>
 * </table>
 * <p>
 *
 * <h4>Differences from java.text.MessageFormat</h4>
 *
 * <p>This class supports both named and numbered arguments,
 * while the regular MessageFormat only supports numbered arguments.
 * Named arguments make patterns more readable.
 *
 * <p>This class implements a more user-friendly apostrophe quoting syntax.
 * In message text, an apostrophe only begins quoting literal text
 * if it immediately precedes a syntax character (mostly {curly braces}).<br>
 * In the regular MessageFormat, an apostrophe always begins quoting,
 * which requires common text like "don't" and "aujourd'hui"
 * to be written with doubled apostrophes like "don''t" and "aujourd''hui".
 * For more details see {@link MessagePattern.ApostropheMode}.
 *
 * <p>This class does not create a ChoiceFormat object for a choiceArg, pluralArg or selectArg
 * but rather handles such arguments itself.
 * The regular MessageFormat does create and use a ChoiceFormat object
 * (<code>new ChoiceFormat(argStyleText)</code>).
 * The regular MessageFormat does not support plural and select arguments at all.
 *
 * <h4>Usage Information</h4>
 *
 * <p>Here are some examples of usage:
 * <blockquote>
 * <pre>
 * Object[] arguments = {
 *     7,
 *     new Date(System.currentTimeMillis()),
 *     "a disturbance in the Force"
 * };
 *
 * String result = MessageFormat.format(
 *     "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}.",
 *     arguments);
 *
 * <em>output</em>: At 12:30 PM on Jul 3, 2053, there was a disturbance
 *           in the Force on planet 7.
 *
 * </pre>
 * </blockquote>
 * Typically, the message format will come from resources, and the
 * arguments will be dynamically set at runtime.
 *
 * <p>Example 2:
 * <blockquote>
 * <pre>
 * Object[] testArgs = { 3, "MyDisk" };
 *
 * MessageFormat form = new MessageFormat(
 *     "The disk \"{1}\" contains {0} file(s).");
 *
 * System.out.println(form.format(testArgs));
 *
 * // output, with different testArgs
 * <em>output</em>: The disk "MyDisk" contains 0 file(s).
 * <em>output</em>: The disk "MyDisk" contains 1 file(s).
 * <em>output</em>: The disk "MyDisk" contains 1,273 file(s).
 * </pre>
 * </blockquote>
 *
 * <p>For messages that include plural forms, you can use a plural argument:
 * <pre>
 * MessageFormat msgFmt = new MessageFormat(
 *     "{num_files, plural, " +
 *     "=0{There are no files on disk \"{disk_name}\".}" +
 *     "=1{There is one file on disk \"{disk_name}\".}" +
 *     "other{There are # files on disk \"{disk_name}\".}}",
 *     Locale.ENGLISH);
 * Map args = new HashMap();
 * args.put("num_files", 0);
 * args.put("disk_name", "MyDisk");
 * System.out.println(msgFmt.format(args));
 * args.put("num_files", 3);
 * System.out.println(msgFmt.format(args));
 *
 * <em>output</em>:
 * There are no files on disk "MyDisk".
 * There are 3 files on "MyDisk".
 * </pre>
 * See {@link PluralFormat} and {@link PluralRules} for details.
 *
 * <h4><a name="synchronization">Synchronization</a></h4>
 *
 * <p>MessageFormats are not synchronized.
 * It is recommended to create separate format instances for each thread.
 * If multiple threads access a format concurrently, it must be synchronized
 * externally.
 *
 * @author       Mark Davis
 * @author       Markus Scherer
 * @stable ICU 3.0
 */
public final class MessageFormat {

  private static final ThreadLocal<CachedNumberFormats> cachedNumberFormats = new ThreadLocal<CachedNumberFormats>() {
    @Override protected CachedNumberFormats initialValue() {
      return new CachedNumberFormats();
    }
  };

  private static final ThreadLocal<MessageFormat> cachedMessageFormat = new ThreadLocal<MessageFormat>() {
    @Override protected MessageFormat initialValue() {
      return new MessageFormat(Locale.ROOT, "");
    }
  };

  private Locale locale;

  /** The MessagePattern which contains the parsed structure of the pattern string. */
  private MessagePattern msgPattern;

  private Format stockDateFormatter;

  // These don't need caching because PluralRulesLoader caches. Though we could switch that round.
  private PluralSelectorProvider pluralProvider;
  private PluralSelectorProvider ordinalProvider;

  private MessageFormat(Locale locale, String pattern) {
    this.locale = locale;
    this.msgPattern = new MessagePattern(pattern);
  }

  private MessageFormat(Locale locale, String pattern, MessagePattern.ApostropheMode mode) {
    this.locale = locale;
    this.msgPattern = new MessagePattern(mode, pattern);
  }

  public static final String format(Locale locale, String pattern, Object... arguments) {
    // Reuse a cached MessageFormat.
    MessageFormat mf = cachedMessageFormat.get();
    mf.locale = locale;
    mf.msgPattern.parse(pattern);

    if (mf.msgPattern.hasNamedArguments()) {
      throw new IllegalArgumentException("named arguments not supported");
    }

    StringBuilder sb = new StringBuilder();
    mf.format(0, 0, arguments, null, sb);
    return sb.toString();
  }

    /**
     * Formats the arguments and writes the result into the
     * StringBuilder, updates the field position.
     *
     * <p>Exactly one of args and argsMap must be null, the other non-null.
     *
     * @param msgStart      Index to msgPattern part to start formatting from.
     * @param pluralNumber  Zero except when formatting a plural argument sub-message
     *                      where a '#' is replaced by the format string for this number.
     * @param args          The formattable objects array. Non-null iff numbered values are used.
     * @param argsMap       The key-value map of formattable objects. Non-null iff named values are used.
     * @param dest          Output parameter to receive the result.
     *                      The result (string & attributes) is appended to existing contents.
     */
    private void format(int msgStart, double pluralNumber,
                        Object[] args, Map<String, Object> argsMap,
                        StringBuilder dest) {
        String msgString=msgPattern.getPatternString();
        int prevIndex=msgPattern.getPart(msgStart).getLimit();
        for(int i=msgStart+1;; ++i) {
            Part part=msgPattern.getPart(i);
            Part.Type type=part.getType();
            int index=part.getIndex();
            dest.append(msgString, prevIndex, index);
            if(type==Part.Type.MSG_LIMIT) {
                return;
            }
            prevIndex=part.getLimit();
            if(type==Part.Type.REPLACE_NUMBER) {
                dest.append(getNumberFormatInstance(MODIFIER_EMPTY, locale).format(pluralNumber));
                continue;
            }
            if(type!=Part.Type.ARG_START) {
                continue;
            }
            int argLimit=msgPattern.getLimitPartIndex(i);
            ArgType argType=part.getArgType();
            part=msgPattern.getPart(++i);
            Object arg;
            String noArg=null;
            Object argId=null;
            if(args!=null) {
                int argNumber=part.getValue();  // ARG_NUMBER
                if(0<=argNumber && argNumber<args.length) {
                    arg=args[argNumber];
                } else {
                    arg=null;
                    noArg="{"+argNumber+"}";
                }
            } else {
                String key;
                if(part.getType()==MessagePattern.Part.Type.ARG_NAME) {
                    key=msgPattern.getSubstring(part);
                } else /* ARG_NUMBER */ {
                    key=Integer.toString(part.getValue());
                }
                argId = key;
                if(argsMap!=null && argsMap.containsKey(key)) {
                    arg=argsMap.get(key);
                } else {
                    arg=null;
                    noArg="{"+key+"}";
                }
            }
            ++i;
            int prevDestLength=dest.length();
            if (noArg != null) {
                dest.append(noArg);
            } else if (arg == null) {
                dest.append("null");
            } else if(argType == ArgType.SIMPLE) {
                // This code is in cacheExplicitFormats upstream, but the cache costs us more than
                // it saves us.
                String explicitType = msgPattern.getSubstring(msgPattern.getPart(i));
                String style = "";
                Part stylePart = msgPattern.getPart(i + 1);
                if (stylePart.getType() == MessagePattern.Part.Type.ARG_STYLE) {
                    style = msgPattern.getSubstring(stylePart);
                }
                Format formatter = createAppropriateFormat(explicitType, style);

                // Handles all ArgType.SIMPLE, and formatters from setFormat() and its siblings.
                if (formatter instanceof ChoiceFormat) {
                    // We only handle nested formats here if they were provided via setFormat() or its siblings.
                    // Otherwise they are not cached and instead handled below according to argType.
                    String subMsgString = formatter.format(arg);
                    if (subMsgString.indexOf('{') >= 0 ||
                            (subMsgString.indexOf('\'') >= 0 && !msgPattern.jdkAposMode())) {
                        MessageFormat subMsgFormat = new MessageFormat(locale, subMsgString);
                        subMsgFormat.format(0, 0, args, argsMap, dest);
                    } else {
                        dest.append(subMsgString);
                    }
                } else {
                    dest.append(formatter.format(arg));
                }
            } else if (argType==ArgType.NONE) {
                if (arg instanceof Number) {
                    // format number if can
                    dest.append(getNumberFormatInstance(MODIFIER_EMPTY, locale).format(arg));
                 } else if (arg instanceof Date) {
                    // format a Date if can
                    if (stockDateFormatter == null) {
                        stockDateFormatter = DateFormat.getDateTimeInstance(
                                DateFormat.SHORT, DateFormat.SHORT, locale);//fix
                    }
                    dest.append(stockDateFormatter.format(arg));
                } else {
                    dest.append(arg.toString());
                }
            } else if(argType==ArgType.CHOICE) {
                if (!(arg instanceof Number)) {
                    throw new IllegalArgumentException("'" + arg + "' is not a Number");
                }
                double number = ((Number)arg).doubleValue();
                int subMsgStart=findChoiceSubMessage(msgPattern, i, number);
                formatComplexSubMessage(subMsgStart, 0, args, argsMap, dest);
            } else if(argType.hasPluralStyle()) {
                if (!(arg instanceof Number)) {
                    throw new IllegalArgumentException("'" + arg + "' is not a Number");
                }
                double number = ((Number)arg).doubleValue();
                PluralSelectorProvider selector;
                if(argType == ArgType.PLURAL) {
                    if (pluralProvider == null) {
                        pluralProvider = new PluralSelectorProvider(locale, PluralType.CARDINAL);
                    }
                    selector = pluralProvider;
                } else {
                    if (ordinalProvider == null) {
                        ordinalProvider = new PluralSelectorProvider(locale, PluralType.ORDINAL);
                    }
                    selector = ordinalProvider;
                }
                int subMsgStart=findPluralSubMessage(msgPattern, i, selector, number);
                double offset=msgPattern.getPluralOffset(i);
                formatComplexSubMessage(subMsgStart, number-offset, args, argsMap, dest);
            } else if(argType==ArgType.SELECT) {
                int subMsgStart=findSelectSubMessage(msgPattern, i, arg.toString());
                formatComplexSubMessage(subMsgStart, 0, args, argsMap, dest);
            } else {
                // This should never happen.
                throw new IllegalStateException("unexpected argType "+argType);
            }
            prevIndex=msgPattern.getPart(argLimit).getLimit();
            i=argLimit;
        }
    }

    /**
     * Finds the PluralFormat sub-message for the given number, or the "other" sub-message.
     * @param pattern A MessagePattern.
     * @param partIndex the index of the first PluralFormat argument style part.
     * @param selector the PluralSelector for mapping the number (minus offset) to a keyword.
     * @param number a number to be matched to one of the PluralFormat argument's explicit values,
     *       or mapped via the PluralSelector.
     * @return the sub-message start part index.
     */
    private static int findPluralSubMessage(
        MessagePattern pattern, int partIndex,
        PluralSelectorProvider selector, double number) {
      int count=pattern.countParts();
      double offset;
      MessagePattern.Part part=pattern.getPart(partIndex);
      if(part.getType().hasNumericValue()) {
        offset=pattern.getNumericValue(part);
        ++partIndex;
      } else {
        offset=0;
      }
      // The keyword is null until we need to match against non-explicit, not-"other" value.
      // Then we get the keyword from the selector.
      // (In other words, we never call the selector if we match against an explicit value,
      // or if the only non-explicit keyword is "other".)
      String keyword=null;
      // When we find a match, we set msgStart>0 and also set this boolean to true
      // to avoid matching the keyword again (duplicates are allowed)
      // while we continue to look for an explicit-value match.
      boolean haveKeywordMatch=false;
      // msgStart is 0 until we find any appropriate sub-message.
      // We remember the first "other" sub-message if we have not seen any
      // appropriate sub-message before.
      // We remember the first matching-keyword sub-message if we have not seen
      // one of those before.
      // (The parser allows [does not check for] duplicate keywords.
      // We just have to make sure to take the first one.)
      // We avoid matching the keyword twice by also setting haveKeywordMatch=true
      // at the first keyword match.
      // We keep going until we find an explicit-value match or reach the end of the plural style.
      int msgStart=0;
      // Iterate over (ARG_SELECTOR [ARG_INT|ARG_DOUBLE] message) tuples
      // until ARG_LIMIT or end of plural-only pattern.
      do {
        part=pattern.getPart(partIndex++);
        MessagePattern.Part.Type type=part.getType();
        if(type==MessagePattern.Part.Type.ARG_LIMIT) {
          break;
        }
        assert type==MessagePattern.Part.Type.ARG_SELECTOR;
        // part is an ARG_SELECTOR followed by an optional explicit value, and then a message
        if(pattern.getPartType(partIndex).hasNumericValue()) {
          // explicit value like "=2"
          part=pattern.getPart(partIndex++);
          if(number==pattern.getNumericValue(part)) {
            // matches explicit value
            return partIndex;
          }
        } else if(!haveKeywordMatch) {
          // plural keyword like "few" or "other"
          // Compare "other" first and call the selector if this is not "other".
          if(pattern.partSubstringMatches(part, "other")) {
            if(msgStart==0) {
              msgStart=partIndex;
              if(keyword!=null && keyword.equals("other")) {
                // This is the first "other" sub-message,
                // and the selected keyword is also "other".
                // Do not match "other" again.
                haveKeywordMatch=true;
              }
            }
          } else {
            if(keyword==null) {
              keyword=selector.select(number-offset);
              if(msgStart!=0 && keyword.equals("other")) {
                // We have already seen an "other" sub-message.
                // Do not match "other" again.
                haveKeywordMatch=true;
                // Skip keyword matching but do getLimitPartIndex().
              }
            }
            if(!haveKeywordMatch && pattern.partSubstringMatches(part, keyword)) {
              // keyword matches
              msgStart=partIndex;
              // Do not match this keyword again.
              haveKeywordMatch=true;
            }
          }
        }
        partIndex=pattern.getLimitPartIndex(partIndex);
      } while(++partIndex<count);
      return msgStart;
    }


    /**
     * Finds the SelectFormat sub-message for the given keyword, or the "other" sub-message.
     * @param pattern A MessagePattern.
     * @param partIndex the index of the first SelectFormat argument style part.
     * @param keyword a keyword to be matched to one of the SelectFormat argument's keywords.
     * @return the sub-message start part index.
     */
    private static int findSelectSubMessage(MessagePattern pattern, int partIndex, String keyword) {
      int count=pattern.countParts();
      int msgStart=0;
      // Iterate over (ARG_SELECTOR, message) pairs until ARG_LIMIT or end of select-only pattern.
      do {
        MessagePattern.Part part=pattern.getPart(partIndex++);
        MessagePattern.Part.Type type=part.getType();
        if(type==MessagePattern.Part.Type.ARG_LIMIT) {
          break;
        }
        assert type==MessagePattern.Part.Type.ARG_SELECTOR;
        // part is an ARG_SELECTOR followed by a message
        if(pattern.partSubstringMatches(part, keyword)) {
          // keyword matches
          return partIndex;
        } else if(msgStart==0 && pattern.partSubstringMatches(part, "other")) {
          msgStart=partIndex;
        }
        partIndex=pattern.getLimitPartIndex(partIndex);
      } while(++partIndex<count);
      return msgStart;
    }

    private void formatComplexSubMessage(
            int msgStart, double pluralNumber,
            Object[] args, Map<String, Object> argsMap,
            StringBuilder dest) {
        if (!msgPattern.jdkAposMode()) {
            format(msgStart, pluralNumber, args, argsMap, dest);
            return;
        }
        // JDK compatibility mode: (see JDK MessageFormat.format() API docs)
        // - remove SKIP_SYNTAX; that is, remove half of the apostrophes
        // - if the result string contains an open curly brace '{' then
        //   instantiate a temporary MessageFormat object and format again;
        //   otherwise just append the result string
        String msgString = msgPattern.getPatternString();
        String subMsgString;
        StringBuilder sb = null;
        int prevIndex = msgPattern.getPart(msgStart).getLimit();
        for (int i = msgStart;;) {
            Part part = msgPattern.getPart(++i);
            Part.Type type = part.getType();
            int index = part.getIndex();
            if (type == Part.Type.MSG_LIMIT) {
                if (sb == null) {
                    subMsgString = msgString.substring(prevIndex, index);
                } else {
                    subMsgString = sb.append(msgString, prevIndex, index).toString();
                }
                break;
            } else if (type == Part.Type.REPLACE_NUMBER || type == Part.Type.SKIP_SYNTAX) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(msgString, prevIndex, index);
                if (type == Part.Type.REPLACE_NUMBER) {
                    sb.append(getNumberFormatInstance(MODIFIER_EMPTY, locale).format(pluralNumber));
                }
                prevIndex = part.getLimit();
            } else if (type == Part.Type.ARG_START) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(msgString, prevIndex, index);
                prevIndex = index;
                i = msgPattern.getLimitPartIndex(i);
                index = msgPattern.getPart(i).getLimit();
                MessagePattern.appendReducedApostrophes(msgString, prevIndex, index, sb);
                prevIndex = index;
            }
        }
        if (subMsgString.indexOf('{') >= 0) {
            MessageFormat subMsgFormat = new MessageFormat(locale, subMsgString, MessagePattern.ApostropheMode.DOUBLE_REQUIRED);
            subMsgFormat.format(0, 0, args, argsMap, dest);
        } else {
            dest.append(subMsgString);
        }
    }

    // This lives here because ICU4J does not have its own ChoiceFormat class.
    /**
     * Finds the ChoiceFormat sub-message for the given number.
     * @param pattern A MessagePattern.
     * @param partIndex the index of the first ChoiceFormat argument style part.
     * @param number a number to be mapped to one of the ChoiceFormat argument's intervals
     * @return the sub-message start part index.
     */
    private static int findChoiceSubMessage(MessagePattern pattern, int partIndex, double number) {
        int count=pattern.countParts();
        int msgStart;
        // Iterate over (ARG_INT|DOUBLE, ARG_SELECTOR, message) tuples
        // until ARG_LIMIT or end of choice-only pattern.
        // Ignore the first number and selector and start the loop on the first message.
        partIndex+=2;
        for(;;) {
            // Skip but remember the current sub-message.
            msgStart=partIndex;
            partIndex=pattern.getLimitPartIndex(partIndex);
            if(++partIndex>=count) {
                // Reached the end of the choice-only pattern.
                // Return with the last sub-message.
                break;
            }
            Part part=pattern.getPart(partIndex++);
            Part.Type type=part.getType();
            if(type==Part.Type.ARG_LIMIT) {
                // Reached the end of the ChoiceFormat style.
                // Return with the last sub-message.
                break;
            }
            // part is an ARG_INT or ARG_DOUBLE
            assert type.hasNumericValue();
            double boundary=pattern.getNumericValue(part);
            // Fetch the ARG_SELECTOR character.
            int selectorIndex=pattern.getPatternIndex(partIndex++);
            char boundaryChar=pattern.getPatternString().charAt(selectorIndex);
            if(boundaryChar=='<' ? !(number>boundary) : !(number>=boundary)) {
                // The number is in the interval between the previous boundary and the current one.
                // Return with the sub-message between them.
                // The !(a>b) and !(a>=b) comparisons are equivalent to
                // (a<=b) and (a<b) except they "catch" NaN.
                break;
            }
        }
        return msgStart;
    }

    /**
     * This provider helps defer instantiation of a PluralRules object
     * until we actually need to select a keyword.
     * For example, if the number matches an explicit-value selector like "=1"
     * we do not need any PluralRules.
     */
    private static final class PluralSelectorProvider {
        private final Locale locale;
        private final PluralType type;
        private PluralRules rules;

        public PluralSelectorProvider(Locale locale, PluralType type) {
            this.locale = locale;
            this.type = type;
        }

        public String select(double number) {
            if (rules == null) {
                rules = PluralRules.forLocale(locale, type);
            }
            return rules.select(number);
        }
    }

    private static final String[] TYPES =
        { "number", "date", "time", /*"spellout", "ordinal", "duration"*/ };
    private static final int
        TYPE_NUMBER = 0,
        TYPE_DATE = 1,
        TYPE_TIME = 2 /*,
        TYPE_SPELLOUT = 3,
        TYPE_ORDINAL = 4,
        TYPE_DURATION = 5*/;

    private static final String[] MODIFIERS =
        {"", "currency", "percent", "integer"};

    private static final int
        MODIFIER_EMPTY = 0,
        MODIFIER_CURRENCY = 1,
        MODIFIER_PERCENT = 2,
        MODIFIER_INTEGER = 3;

    private static final String[] DATE_MODIFIERS =
        {"", "short", "medium", "long", "full"};

    private static final int
        DATE_MODIFIER_EMPTY = 0,
        DATE_MODIFIER_SHORT = 1,
        DATE_MODIFIER_MEDIUM = 2,
        DATE_MODIFIER_LONG = 3,
        DATE_MODIFIER_FULL = 4;

    private NumberFormat getNumberFormatInstance(int kind, Locale locale) {
      return cachedNumberFormats.get().get(kind, locale);
    }

    private static class CachedNumberFormats {
      private Locale cachedLocale;

      private NumberFormat regular;
      private NumberFormat currency;
      private NumberFormat percent;
      private NumberFormat integer;

      public NumberFormat get(int kind, Locale desiredLocale) {
        if (!desiredLocale.equals(cachedLocale)) {
          cachedLocale = desiredLocale;
          switchLocale();
        }
        if (kind == MODIFIER_EMPTY) {
          return regular;
        } else if (kind == MODIFIER_CURRENCY) {
          return currency;
        } else if (kind == MODIFIER_PERCENT) {
          return percent;
        } else if (kind == MODIFIER_INTEGER) {
          return integer;
        } else {
          throw new AssertionError();
        }
      }

      private void switchLocale() {
        // TODO: we might want to instantiate these more lazily.
        regular = NumberFormat.getInstance(cachedLocale);
        currency = NumberFormat.getCurrencyInstance(cachedLocale);
        percent = NumberFormat.getPercentInstance(cachedLocale);
        integer = NumberFormat.getIntegerInstance(cachedLocale);
      }
    }

    // Creates an appropriate Format object for the type and style passed.
    // Both arguments cannot be null.
    private Format createAppropriateFormat(String type, String style) {
        Format newFormat = null;
        int subformatType  = findKeyword(type, TYPES);
        switch (subformatType){
        case TYPE_NUMBER:
            switch (findKeyword(style, MODIFIERS)) {
            case MODIFIER_EMPTY:
            case MODIFIER_CURRENCY:
            case MODIFIER_PERCENT:
            case MODIFIER_INTEGER:
                newFormat = getNumberFormatInstance(findKeyword(style, MODIFIERS), locale);
                break;
            default: // pattern
                newFormat = new DecimalFormat(style, new DecimalFormatSymbols(locale));
                break;
            }
            break;
        case TYPE_DATE:
            switch (findKeyword(style, DATE_MODIFIERS)) {
            case DATE_MODIFIER_EMPTY:
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                break;
            case DATE_MODIFIER_SHORT:
                newFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
                break;
            case DATE_MODIFIER_MEDIUM:
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                break;
            case DATE_MODIFIER_LONG:
                newFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
                break;
            case DATE_MODIFIER_FULL:
                newFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
                break;
            default:
                newFormat = new SimpleDateFormat(style, locale);
                break;
            }
            break;
        case TYPE_TIME:
            switch (findKeyword(style, DATE_MODIFIERS)) {
            case DATE_MODIFIER_EMPTY:
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
                break;
            case DATE_MODIFIER_SHORT:
                newFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
                break;
            case DATE_MODIFIER_MEDIUM:
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
                break;
            case DATE_MODIFIER_LONG:
                newFormat = DateFormat.getTimeInstance(DateFormat.LONG, locale);
                break;
            case DATE_MODIFIER_FULL:
                newFormat = DateFormat.getTimeInstance(DateFormat.FULL, locale);
                break;
            default:
                newFormat = new SimpleDateFormat(style, locale);
                break;
            }
            break;
/* TODO: unimplemented
        case TYPE_SPELLOUT:
            {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(locale,
                        RuleBasedNumberFormat.SPELLOUT);
                String ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    }
                    catch (Exception e) {
                        // warn invalid ruleset
                    }
                }
                newFormat = rbnf;
                break;
            }
        case TYPE_ORDINAL:
            {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(locale,
                        RuleBasedNumberFormat.ORDINAL);
                String ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    }
                    catch (Exception e) {
                        // warn invalid ruleset
                    }
                }
                newFormat = rbnf;
                break;
            }
        case TYPE_DURATION:
            {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(locale,
                        RuleBasedNumberFormat.DURATION);
                String ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    }
                    catch (Exception e) {
                        // warn invalid ruleset
                    }
                }
                newFormat = rbnf;
                break;
            }
*/
        default:
            throw new IllegalArgumentException("Unknown format type \"" + type + "\"");
        }
        return newFormat;
    }

    private static final int findKeyword(String s, String[] list) {
        s = PatternProps.trimWhiteSpace(s).toLowerCase(Locale.ROOT);
        for (int i = 0; i < list.length; ++i) {
            if (s.equals(list[i]))
                return i;
        }
        return -1;
    }
}
