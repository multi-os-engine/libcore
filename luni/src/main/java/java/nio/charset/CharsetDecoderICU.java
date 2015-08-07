/**
*******************************************************************************
* Copyright (C) 1996-2006, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/
 /**
  * A JNI interface for ICU converters.
  *
  *
  * @author Ram Viswanadha, IBM
  */
package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

final class CharsetDecoderICU extends CharsetDecoder {
    private static final com.ibm.icu.charset.CharsetProviderICU providerICU =
            new com.ibm.icu.charset.CharsetProviderICU();

    /* ICU decoder instance that we proxy to */
    private final CharsetDecoder charsetDecoder;

    private CharsetDecoderICU(Charset cs, CharsetDecoder charsetDecoder, float avgCharsPerByte, float maxCharsPerByte) {
        super(cs, avgCharsPerByte, maxCharsPerByte);
        this.charsetDecoder = charsetDecoder;
    }

    public static CharsetDecoderICU newInstance(Charset cs, String icuCanonicalName) {
        Charset icuCharset = providerICU.charsetForName(icuCanonicalName);
        if (icuCharset == null) {
            throw new IllegalArgumentException("Missing ICU charset for: " + icuCanonicalName);
        }
        CharsetDecoder charsetDecoder = icuCharset.newDecoder();

        return new CharsetDecoderICU(cs, charsetDecoder, charsetDecoder.averageCharsPerByte(),
                charsetDecoder.maxCharsPerByte());
    }

    @Override protected void implReplaceWith(String newReplacement) {
        charsetDecoder.implReplaceWith(newReplacement);
     }

    @Override protected final void implOnMalformedInput(CodingErrorAction newAction) {
        charsetDecoder.implOnMalformedInput(newAction);
    }

    @Override protected final void implOnUnmappableCharacter(CodingErrorAction newAction) {
        charsetDecoder.implOnUnmappableCharacter(newAction);
    }

    @Override protected void implReset() {
        charsetDecoder.implReset();
    }

    @Override protected final CoderResult implFlush(CharBuffer out) {
        return charsetDecoder.implFlush(out);
    }

    @Override protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
        if (in == null || out == null) {
            throw new NullPointerException();
        }
        return charsetDecoder.decodeLoop(in, out);
    }
}
