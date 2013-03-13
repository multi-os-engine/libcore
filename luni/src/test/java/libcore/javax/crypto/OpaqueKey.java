/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.javax.crypto;

import java.security.Key;

/**
 * "Opaque" {@link Key} which does not provide structured access to the underlying key material.
 */
public class OpaqueKey implements Key {
    private final String mAlgorithm;
    private final byte[] mEncoded;
    private final String mFormat;

    /**
     * Constructs a new {@code OpaqueKey} instance with the specified algorithm, encoded form, and
     * format of the encoded form.
     *
     * @param encoded encoded form or {@code null} if not supported.
     * @param format format of the encoded form or {@code null} if encoded form not supported.
     */
    public OpaqueKey(String algorithm, byte[] encoded, String format) {
        mAlgorithm = algorithm;
        mEncoded = encoded;
        mFormat = format;
    }

    @Override
    public String getAlgorithm() {
        return mAlgorithm;
    }

    @Override
    public String getFormat() {
        return mFormat;
    }

    @Override
    public byte[] getEncoded() {
        return mEncoded;
    }
}