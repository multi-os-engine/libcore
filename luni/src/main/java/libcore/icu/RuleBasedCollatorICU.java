/**
*******************************************************************************
* Copyright (C) 1996-2005, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*
*******************************************************************************
*/

package libcore.icu;

import com.ibm.icu.text.CollationElementIterator;
import com.ibm.icu.text.RuleBasedCollator;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.CollationKey;
import java.text.ParseException;
import java.util.Locale;

public final class RuleBasedCollatorICU implements Cloneable {
    // The ICU RuleBasedCollator
    RuleBasedCollator ruleBasedCollator;

    /**
     * Create a new instance of RuleBasedCollatorICU which proxies to ICU's RuleBasedCollator.
     * @param rules Rules for building the collation table.
     * @throws ParseException If the rules could not be parsed.
     * @throws IOException If there was an error reading the data files.
     */
    public RuleBasedCollatorICU(String rules) throws ParseException, IOException {
        try {
            ruleBasedCollator = new RuleBasedCollator(rules);
        } catch (Exception e) {
            // The constructor of ICU's RuleBasedCollator throws an IllegalArgumentException
            // if the rules passed in are null.
            if (e instanceof IllegalArgumentException) {
                throw new NullPointerException("Rules cannot be null");
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            if (e instanceof ParseException) {
                throw (ParseException) e;
            }
            throw new IllegalStateException("ICU threw an unexpected exception: " + e.getMessage());
        }
    }

    public RuleBasedCollatorICU(Locale locale) {
        ruleBasedCollator = (RuleBasedCollator) RuleBasedCollator.getInstance(locale);
    }

    private RuleBasedCollatorICU (RuleBasedCollator collator) {
        this.ruleBasedCollator = collator;
    }

    public Object clone() {
        return new RuleBasedCollatorICU(ruleBasedCollator.cloneAsThawed());
    }

    public int compare(String source, String target) {
        return ruleBasedCollator.compare(source, target);
    }

    public int getDecomposition() {
        return ruleBasedCollator.getDecomposition();
    }

    public void setDecomposition(int mode) {
        ruleBasedCollator.setDecomposition(mode);
    }

    public int getStrength() {
        return ruleBasedCollator.getStrength();
    }

    public void setStrength(int strength) {
        ruleBasedCollator.setStrength(strength);
    }

    public CollationKey getCollationKey(String source) {
        if (source == null) {
            return null;
        }
        return new CollationKeyICU(source, ruleBasedCollator.getCollationKey(source).toByteArray());
    }

    public String getRules() {
        return ruleBasedCollator.getRules();
    }

    public CollationElementIterator getCollationElementIterator(String source) {
        return ruleBasedCollator.getCollationElementIterator(source);
    }

    public CollationElementIterator getCollationElementIterator(CharacterIterator it) {
        // We only implement the String-based API, so build a string from the iterator.
        return getCollationElementIterator(characterIteratorToString(it));
    }

    private String characterIteratorToString(CharacterIterator it) {
        StringBuilder result = new StringBuilder();
        for (char ch = it.current(); ch != CharacterIterator.DONE; ch = it.next()) {
            result.append(ch);
        }
        return result.toString();
    }

    @Override public int hashCode() {
        return 42; // No-one uses RuleBasedCollatorICU as a hash key.
    }

    public boolean equals(String source, String target) {
        return (compare(source, target) == 0);
    }

    @Override public boolean equals(Object object) {
        if (object ==  this) {
            return true;
        }
        if (!(object instanceof RuleBasedCollatorICU)) {
            return false;
        }
        RuleBasedCollatorICU rhs = (RuleBasedCollatorICU) object;
        return getRules().equals(rhs.getRules()) &&
                getStrength() == rhs.getStrength() &&
                getDecomposition() == rhs.getDecomposition();
    }
}
