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

package java.net;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import libcore.net.UriCodec;

/**
 * This class is used to decode a string which is encoded in the {@code
 * application/x-www-form-urlencoded} MIME content type.
 */
public class URLDecoder {
    /**
     * Decodes the argument which is assumed to be encoded in the {@code
     * x-www-form-urlencoded} MIME content type.
     * <p>
     *'+' will be converted to space, '%' and two following hex digit
     * characters are converted to the equivalent byte value. All other
     * characters are passed through unmodified. For example "A+B+C %24%25" ->
     * "A B C $%".
     *
     * @param s
     *            the encoded string.
     * @return the decoded clear-text representation of the given string.
     * @deprecated Use {@link #decode(String, String)} instead.
     */
    @Deprecated
    public static String decode(String s) {
        return UriCodec.decode(s, true, Charset.defaultCharset(), true);
    }

    /**
     * Decodes the argument which is assumed to be encoded in the {@code
     * x-www-form-urlencoded} MIME content type, assuming the given {@code charsetName}.
     *
     *'<p>+' will be converted to space, '%' and two following hex digit
     * characters are converted to the equivalent byte value. All other
     * characters are passed through unmodified. For example "A+B+C %24%25" ->
     * "A B C $%".
     *
     * @throws UnsupportedEncodingException if {@code charsetName} is not supported.
     */
    public static String decode(String s, String charsetName) throws UnsupportedEncodingException {
        return UriCodec.decode(s, true, Charset.forName(charsetName), true);
    }

    /**
     * Decodes an {@code application/x-www-form-urlencoded} string using
     * a specific {@linkplain java.nio.charset.Charset Charset}.
     * The supplied charset is used to determine
     * what characters are represented by any consecutive sequences of the
     * form "<i>{@code %xy}</i>".
     * <p>
     * <em><strong>Note:</strong> The <a href=
     * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * World Wide Web Consortium Recommendation</a> states that
     * UTF-8 should be used. Not doing so may introduce
     * incompatibilities.</em>
     *
     * @implNote This implementation will throw an {@link java.lang.IllegalArgumentException}
     * when illegal strings are encountered.
     *
     * @param s the {@code String} to decode
     * @param charset the given charset
     * @return the newly decoded {@code String}
     * @throws NullPointerException if {@code s} or {@code charset} is {@code null}
     * @throws IllegalArgumentException if the implementation encounters illegal
     * characters
     * @see URLEncoder#encode(java.lang.String, java.nio.charset.Charset)
     * @since 10
     */
    public static String decode(String s, Charset charset) {
        return UriCodec.decode(s, true, charset, true);
    }
}
