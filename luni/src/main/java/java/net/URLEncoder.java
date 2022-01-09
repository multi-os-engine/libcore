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
import java.nio.charset.StandardCharsets;
import libcore.net.UriCodec;

/**
 * This class is used to encode a string using the format required by
 * {@code application/x-www-form-urlencoded} MIME content type.
 *
 * <p>All characters except letters ('a'..'z', 'A'..'Z') and numbers ('0'..'9')
 * and characters '.', '-', '*', '_' are converted into their hexadecimal value
 * prepended by '%'. For example: '#' -> %23. In addition, spaces are
 * substituted by '+'.
 */
public class URLEncoder {
    private URLEncoder() {}

    static UriCodec ENCODER = new UriCodec() {
        @Override protected boolean isRetained(char c) {
            return " .-*_".indexOf(c) != -1;
        }
    };

    /**
     * Equivalent to {@code encode(s, "UTF-8")}.
     *
     * @deprecated Use {@link #encode(String, String)} instead.
     */
    @Deprecated
    public static String encode(String s) {
        return ENCODER.encode(s, StandardCharsets.UTF_8);
    }

    /**
     * Encodes {@code s} using the {@link Charset} named by {@code charsetName}.
     */
    public static String encode(String s, String charsetName) throws UnsupportedEncodingException {
        return ENCODER.encode(s, Charset.forName(charsetName));
    }

    /**
     * Translates a string into {@code application/x-www-form-urlencoded}
     * format using a specific {@linkplain java.nio.charset.Charset Charset}.
     * This method uses the supplied charset to obtain the bytes for unsafe
     * characters.
     * <p>
     * <em><strong>Note:</strong> The <a href=
     * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * World Wide Web Consortium Recommendation</a> states that
     * UTF-8 should be used. Not doing so may introduce incompatibilities.</em>
     *
     * @param   s   {@code String} to be translated.
     * @param charset the given charset
     * @return  the translated {@code String}.
     * @throws NullPointerException if {@code s} or {@code charset} is {@code null}.
     * @see URLDecoder#decode(java.lang.String, java.nio.charset.Charset)
     * @since 10
     */
    public static String encode(String s, Charset charset) {
        return ENCODER.encode(s, charset);
    }
}
