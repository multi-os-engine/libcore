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
import com.android.i18n.SelectFormat;
//import com.ibm.icu.text.PluralFormat.PluralSelector;
//import com.ibm.icu.text.PluralRules.PluralType;

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

    /**
     * Constructs a MessageFormat for the specified locale and
     * pattern.
     *
     * @param pattern the pattern for this message format
     * @param locale the locale for this message format
     * @exception IllegalArgumentException if the pattern is invalid
     * @stable ICU 3.0
     */
    public MessageFormat(String pattern, Locale locale) {
        this.ulocale = locale;
        this.msgPattern = new MessagePattern(pattern);
        // Cache the formats that are explicitly mentioned in the message pattern.
        cacheExplicitFormats();
    }

    private MessageFormat(String pattern, Locale locale, MessagePattern.ApostropheMode mode) {
        this.ulocale = locale;
        this.msgPattern = new MessagePattern(mode, pattern);
        // Cache the formats that are explicitly mentioned in the message pattern.
        cacheExplicitFormats();
    }

    /**
     * Formats an array of objects and appends the <code>MessageFormat</code>'s
     * pattern, with arguments replaced by the formatted objects, to the
     * provided <code>StringBuffer</code>.
     * <p>
     * The text substituted for the individual format elements is derived from
     * the current subformat of the format element and the
     * <code>arguments</code> element at the format element's argument index
     * as indicated by the first matching line of the following table. An
     * argument is <i>unavailable</i> if <code>arguments</code> is
     * <code>null</code> or has fewer than argumentIndex+1 elements.  When
     * an argument is unavailable no substitution is performed.
     * <p>
     * <table border=1>
     *    <tr>
     *       <th>argType or Format
     *       <th>value object
     *       <th>Formatted Text
     *    <tr>
     *       <td><i>any</i>
     *       <td><i>unavailable</i>
     *       <td><code>"{" + argNameOrNumber + "}"</code>
     *    <tr>
     *       <td><i>any</i>
     *       <td><code>null</code>
     *       <td><code>"null"</code>
     *    <tr>
     *       <td>custom Format <code>!= null</code>
     *       <td><i>any</i>
     *       <td><code>customFormat.format(argument)</code>
     *    <tr>
     *       <td>noneArg, or custom Format <code>== null</code>
     *       <td><code>instanceof Number</code>
     *       <td><code>NumberFormat.getInstance(getLocale()).format(argument)</code>
     *    <tr>
     *       <td>noneArg, or custom Format <code>== null</code>
     *       <td><code>instanceof Date</code>
     *       <td><code>DateFormat.getDateTimeInstance(DateFormat.SHORT,
     *           DateFormat.SHORT, getLocale()).format(argument)</code>
     *    <tr>
     *       <td>noneArg, or custom Format <code>== null</code>
     *       <td><code>instanceof String</code>
     *       <td><code>argument</code>
     *    <tr>
     *       <td>noneArg, or custom Format <code>== null</code>
     *       <td><i>any</i>
     *       <td><code>argument.toString()</code>
     *    <tr>
     *       <td>complexArg
     *       <td><i>any</i>
     *       <td>result of recursive formatting of a selected sub-message
     * </table>
     * <p>
     *
     * This method is only supported when the format does not use named
     * arguments, otherwise an IllegalArgumentException is thrown.
     *
     * @param arguments an array of objects to be formatted and substituted.
     * @param result where text is appended.
     * @throws IllegalArgumentException if a value in the
     *         <code>arguments</code> array is not of the type
     *         expected by the corresponding argument or custom Format object.
     * @throws IllegalArgumentException if this format uses named arguments
     * @stable ICU 3.0
     */
    public final StringBuffer format(Object[] arguments, StringBuffer result) {
        format(arguments, null, new AppendableWrapper(result));
        return result;
    }

    /**
     * Formats a map of objects and appends the <code>MessageFormat</code>'s
     * pattern, with arguments replaced by the formatted objects, to the
     * provided <code>StringBuffer</code>.
     * <p>
     * The text substituted for the individual format elements is derived from
     * the current subformat of the format element and the
     * <code>arguments</code> value corresponding to the format element's
     * argument name.
     * <p>
     * A numbered pattern argument is matched with a map key that contains that number
     * as an ASCII-decimal-digit string (without leading zero).
     * <p>
     * An argument is <i>unavailable</i> if <code>arguments</code> is
     * <code>null</code> or does not have a value corresponding to an argument
     * name in the pattern.  When an argument is unavailable no substitution
     * is performed.
     *
     * @param arguments a map of objects to be formatted and substituted.
     * @param result where text is appended.
     * @throws IllegalArgumentException if a value in the
     *         <code>arguments</code> array is not of the type
     *         expected by the corresponding argument or custom Format object.
     * @return the passed-in StringBuffer
     * @stable ICU 3.8
     */
    public final StringBuffer format(Map<String, Object> arguments, StringBuffer result) {
        format(null, arguments, new AppendableWrapper(result));
        return result;
    }

    /**
     * Creates a MessageFormat with the given pattern and uses it
     * to format the given arguments. This is equivalent to
     * <blockquote>
     *     <code>(new {@link #MessageFormat(String) MessageFormat}(pattern)).{@link
     *     #format(java.lang.Object[], java.lang.StringBuffer)
     *     format}(arguments, new StringBuffer()).toString()</code>
     * </blockquote>
     *
     * @throws IllegalArgumentException if the pattern is invalid
     * @throws IllegalArgumentException if a value in the
     *         <code>arguments</code> array is not of the type
     *         expected by the corresponding argument or custom Format object.
     * @throws IllegalArgumentException if this format uses named arguments
     * @stable ICU 3.0
     */
    public static String format(String pattern, Object... arguments) {
        return new MessageFormat(pattern, Locale.getDefault()).format(arguments);
    }

    /**
     * Creates a MessageFormat with the given pattern and uses it to
     * format the given arguments.  The pattern must identify arguments
     * by name instead of by number.
     * <p>
     * @throws IllegalArgumentException if the pattern is invalid
     * @throws IllegalArgumentException if a value in the
     *         <code>arguments</code> array is not of the type
     *         expected by the corresponding argument or custom Format object.
     * @see #format(Map, StringBuffer)
     * @see #format(String, Object[])
     * @stable ICU 3.8
     */
    public static String format(String pattern, Map<String, Object> arguments) {
        return new MessageFormat(pattern, Locale.getDefault()).format(arguments);
    }

    public final String format(Object object) {
        return format(object, new StringBuffer()).toString();
    }

    // Overrides
    /**
     * Formats a map or array of objects and appends the <code>MessageFormat</code>'s
     * pattern, with format elements replaced by the formatted objects, to the
     * provided <code>StringBuffer</code>.
     * A map must be provided if this format uses named arguments, otherwise
     * an IllegalArgumentException will be thrown.
     * @param arguments a map or array of objects to be formatted
     * @param result where text is appended
     * @throws IllegalArgumentException if an argument in
     *         <code>arguments</code> is not of the type
     *         expected by the format element(s) that use it
     * @throws IllegalArgumentException if <code>arguments<code> is
     *         an array of Object and this format uses named arguments
     * @stable ICU 3.0
     */
    public final StringBuffer format(Object arguments, StringBuffer result) {
        format(arguments, new AppendableWrapper(result));
        return result;
    }

    // ===========================privates============================

    /**
     * The locale to use for formatting numbers and dates.
     */
    private Locale ulocale;

    /**
     * The MessagePattern which contains the parsed structure of the pattern string.
     */
    private MessagePattern msgPattern;

    /**
     * Cached formatters so we can just use them whenever needed instead of creating
     * them from scratch every time.
     */
    private Map<Integer, Format> cachedFormatters;

    /**
     * Stock formatters. Those are used when a format is not explicitly mentioned in
     * the message. The format is inferred from the argument.
     */
    private Format stockDateFormatter;
    private Format stockNumberFormatter;

//TODO    private PluralSelectorProvider pluralProvider;
//TODO    private PluralSelectorProvider ordinalProvider;

    /**
     * Formats the arguments and writes the result into the
     * AppendableWrapper, updates the field position.
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
                        AppendableWrapper dest) {
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
                if (stockNumberFormatter == null) {
                    stockNumberFormatter = NumberFormat.getInstance(ulocale);
                }
                dest.formatAndAppend(stockNumberFormatter, pluralNumber);
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
            int prevDestLength=dest.length;
            Format formatter = null;
            if (noArg != null) {
                dest.append(noArg);
            } else if (arg == null) {
                dest.append("null");
            } else if(cachedFormatters!=null && (formatter=cachedFormatters.get(i - 2))!=null) {
                // Handles all ArgType.SIMPLE, and formatters from setFormat() and its siblings.
                if (    formatter instanceof ChoiceFormat ||
//TODO                        formatter instanceof PluralFormat ||
                        formatter instanceof SelectFormat) {
                    // We only handle nested formats here if they were provided via setFormat() or its siblings.
                    // Otherwise they are not cached and instead handled below according to argType.
                    String subMsgString = formatter.format(arg);
                    if (subMsgString.indexOf('{') >= 0 ||
                            (subMsgString.indexOf('\'') >= 0 && !msgPattern.jdkAposMode())) {
                        MessageFormat subMsgFormat = new MessageFormat(subMsgString, ulocale);
                        subMsgFormat.format(0, 0, args, argsMap, dest);
                    } else {
                        dest.append(subMsgString);
                    }
                } else {
                    dest.formatAndAppend(formatter, arg);
                }
            } else if(
                    argType==ArgType.NONE ||
                    (cachedFormatters!=null && cachedFormatters.containsKey(i - 2))) {
                // ArgType.NONE, or
                // any argument which got reset to null via setFormat() or its siblings.
                if (arg instanceof Number) {
                    // format number if can
                    if (stockNumberFormatter == null) {
                        stockNumberFormatter = NumberFormat.getInstance(ulocale);
                    }
                    dest.formatAndAppend(stockNumberFormatter, arg);
                 } else if (arg instanceof Date) {
                    // format a Date if can
                    if (stockDateFormatter == null) {
                        stockDateFormatter = DateFormat.getDateTimeInstance(
                                DateFormat.SHORT, DateFormat.SHORT, ulocale);//fix
                    }
                    dest.formatAndAppend(stockDateFormatter, arg);
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
                throw new UnsupportedOperationException();
/* TODO
                PluralSelector selector;
                if(argType == ArgType.PLURAL) {
                    if (pluralProvider == null) {
                        pluralProvider = new PluralSelectorProvider(ulocale, PluralType.CARDINAL);
                    }
                    selector = pluralProvider;
                } else {
                    if (ordinalProvider == null) {
                        ordinalProvider = new PluralSelectorProvider(ulocale, PluralType.ORDINAL);
                    }
                    selector = ordinalProvider;
                }
                int subMsgStart=PluralFormat.findSubMessage(msgPattern, i, selector, number);
                double offset=msgPattern.getPluralOffset(i);
                formatComplexSubMessage(subMsgStart, number-offset, args, argsMap, dest);
*/
            } else if(argType==ArgType.SELECT) {
                int subMsgStart=SelectFormat.findSubMessage(msgPattern, i, arg.toString());
                formatComplexSubMessage(subMsgStart, 0, args, argsMap, dest);
            } else {
                // This should never happen.
                throw new IllegalStateException("unexpected argType "+argType);
            }
            prevIndex=msgPattern.getPart(argLimit).getLimit();
            i=argLimit;
        }
    }

    private void formatComplexSubMessage(
            int msgStart, double pluralNumber,
            Object[] args, Map<String, Object> argsMap,
            AppendableWrapper dest) {
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
                    if (stockNumberFormatter == null) {
                        stockNumberFormatter = NumberFormat.getInstance(ulocale);
                    }
                    sb.append(stockNumberFormatter.format(pluralNumber));
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
            MessageFormat subMsgFormat = new MessageFormat(subMsgString, ulocale, MessagePattern.ApostropheMode.DOUBLE_REQUIRED);
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
/*
//TODO
    private static final class PluralSelectorProvider implements PluralFormat.PluralSelector {
        public PluralSelectorProvider(Locale loc, PluralType type) {
            locale=loc;
            this.type=type;
        }
        public String select(double number) {
            if(rules == null) {
                rules = PluralRules.forLocale(locale, type);
            }
            return rules.select(number);
        }
        private Locale locale;
        private PluralRules rules;
        private PluralType type;
    }
    */

    @SuppressWarnings("unchecked")
    private void format(Object arguments, AppendableWrapper result) {
        if ((arguments == null || arguments instanceof Map)) {
            format(null, (Map<String, Object>)arguments, result);
        } else {
            format((Object[])arguments, null, result);
        }
    }

    /**
     * Internal routine used by format.
     *
     * @throws IllegalArgumentException if an argument in the
     *         <code>arguments</code> map is not of the type
     *         expected by the format element(s) that use it.
     */
    private void format(Object[] arguments, Map<String, Object> argsMap, AppendableWrapper dest) {
        if (arguments != null && msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException(
                "This method is not available in MessageFormat objects " +
                "that use alphanumeric argument names.");
        }
        format(0, 0, arguments, argsMap, dest);
    }

    private static final String[] typeList =
        { "number", "date", "time", "spellout", "ordinal", "duration" };
    private static final int
        TYPE_NUMBER = 0,
        TYPE_DATE = 1,
        TYPE_TIME = 2,
        TYPE_SPELLOUT = 3,
        TYPE_ORDINAL = 4,
        TYPE_DURATION = 5;

    private static final String[] modifierList =
        {"", "currency", "percent", "integer"};

    private static final int
        MODIFIER_EMPTY = 0,
        MODIFIER_CURRENCY = 1,
        MODIFIER_PERCENT = 2,
        MODIFIER_INTEGER = 3;

    private static final String[] dateModifierList =
        {"", "short", "medium", "long", "full"};

    private static final int
        DATE_MODIFIER_EMPTY = 0,
        DATE_MODIFIER_SHORT = 1,
        DATE_MODIFIER_MEDIUM = 2,
        DATE_MODIFIER_LONG = 3,
        DATE_MODIFIER_FULL = 4;

    // Creates an appropriate Format object for the type and style passed.
    // Both arguments cannot be null.
    private Format createAppropriateFormat(String type, String style) {
        Format newFormat = null;
        int subformatType  = findKeyword(type, typeList);
        switch (subformatType){
        case TYPE_NUMBER:
            switch (findKeyword(style, modifierList)) {
            case MODIFIER_EMPTY:
                newFormat = NumberFormat.getInstance(ulocale);
                break;
            case MODIFIER_CURRENCY:
                newFormat = NumberFormat.getCurrencyInstance(ulocale);
                break;
            case MODIFIER_PERCENT:
                newFormat = NumberFormat.getPercentInstance(ulocale);
                break;
            case MODIFIER_INTEGER:
                newFormat = NumberFormat.getIntegerInstance(ulocale);
                break;
            default: // pattern
                newFormat = new DecimalFormat(style, new DecimalFormatSymbols(ulocale));
                break;
            }
            break;
        case TYPE_DATE:
            switch (findKeyword(style, dateModifierList)) {
            case DATE_MODIFIER_EMPTY:
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_SHORT:
                newFormat = DateFormat.getDateInstance(DateFormat.SHORT, ulocale);
                break;
            case DATE_MODIFIER_MEDIUM:
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_LONG:
                newFormat = DateFormat.getDateInstance(DateFormat.LONG, ulocale);
                break;
            case DATE_MODIFIER_FULL:
                newFormat = DateFormat.getDateInstance(DateFormat.FULL, ulocale);
                break;
            default:
                newFormat = new SimpleDateFormat(style, ulocale);
                break;
            }
            break;
        case TYPE_TIME:
            switch (findKeyword(style, dateModifierList)) {
            case DATE_MODIFIER_EMPTY:
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_SHORT:
                newFormat = DateFormat.getTimeInstance(DateFormat.SHORT, ulocale);
                break;
            case DATE_MODIFIER_MEDIUM:
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_LONG:
                newFormat = DateFormat.getTimeInstance(DateFormat.LONG, ulocale);
                break;
            case DATE_MODIFIER_FULL:
                newFormat = DateFormat.getTimeInstance(DateFormat.FULL, ulocale);
                break;
            default:
                newFormat = new SimpleDateFormat(style, ulocale);
                break;
            }
            break;
        case TYPE_SPELLOUT:
            {
                throw new UnsupportedOperationException();
/* TODO                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(ulocale,
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
*/
            }
        case TYPE_ORDINAL:
            {
                throw new UnsupportedOperationException();
/* TODO               RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(ulocale,
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
*/
            }
        case TYPE_DURATION:
            {
                throw new UnsupportedOperationException();
/* TODO                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(ulocale,
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
*/
            }
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

    private void cacheExplicitFormats() {
        if (cachedFormatters != null) {
            cachedFormatters.clear();
        }
        // The last two "parts" can at most be ARG_LIMIT and MSG_LIMIT
        // which we need not examine.
        int limit = msgPattern.countParts() - 2;
        // This loop starts at part index 1 because we do need to examine
        // ARG_START parts. (But we can ignore the MSG_START.)
        for(int i=1; i < limit; ++i) {
            Part part = msgPattern.getPart(i);
            if(part.getType()!=Part.Type.ARG_START) {
                continue;
            }
            ArgType argType=part.getArgType();
            if(argType != ArgType.SIMPLE) {
                continue;
            }
            int index = i;
            i += 2;
            String explicitType = msgPattern.getSubstring(msgPattern.getPart(i++));
            String style = "";
            if ((part = msgPattern.getPart(i)).getType() == MessagePattern.Part.Type.ARG_STYLE) {
                style = msgPattern.getSubstring(part);
                ++i;
            }
            Format formatter = createAppropriateFormat(explicitType, style);
            setArgStartFormat(index, formatter);
        }
    }

    /**
     * Sets a formatter for a MessagePattern ARG_START part index.
     */
    private void setArgStartFormat(int argStart, Format formatter) {
        if (cachedFormatters == null) {
            cachedFormatters = new HashMap<Integer, Format>();
        }
        cachedFormatters.put(argStart, formatter);
    }

    /**
     * Convenience wrapper for Appendable, tracks the result string length.
     * Also, Appendable throws IOException, and we turn that into a RuntimeException
     * so that we need no throws clauses.
     */
    private static final class AppendableWrapper {
        public AppendableWrapper(StringBuilder sb) {
            app = sb;
            length = sb.length();
        }

        public AppendableWrapper(StringBuffer sb) {
            app = sb;
            length = sb.length();
        }

        public void append(CharSequence s) {
            try {
                app.append(s);
                length += s.length();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void append(CharSequence s, int start, int limit) {
            try {
                app.append(s, start, limit);
                length += limit - start;
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void formatAndAppend(Format formatter, Object arg) {
            append(formatter.format(arg));
        }

        private Appendable app;
        private int length;
    }
}
