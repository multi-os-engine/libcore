/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class GCMParameterSpec implements AlgorithmParameterSpec {
    private final int tagLen;

    private final byte[] iv;

    /**
     * Creates a new {@code GCMParameterSpec} instance from the specified
     * Initial Vector (IV) from buffer {@code iv} and a tag length of
     * {@code tagLen} in bits.
     *
     * @throws IllegalArgumentException if the specified {@code iv} is null or
     *             {@code offset} and {@code byteCount} do not specify a valid
     *             chunk in the specified buffer.
     */
    public GCMParameterSpec(int tagLen, byte[] iv) {
        if (tagLen < 0) {
            throw new IllegalArgumentException("tag should be a non-negative integer");
        }
        if (iv == null) {
            throw new IllegalArgumentException("iv == null");
        }
        this.tagLen = tagLen;
        this.iv = new byte[iv.length];
        System.arraycopy(iv, 0, this.iv, 0, iv.length);
    }

    /**
     * Creates a new {@code GCMParameterSpec} instance with the Initial Vector
     * (IV) of {@code byteCount} bytes from the specified buffer {@code iv}
     * starting at {@code offset} and a tag length of {@code tagLen} in bits.
     *
     * @throws IllegalArgumentException if the specified {@code iv} is null or
     *             {@code offset} and {@code byteCount} do not specify a valid
     *             chunk in the specified buffer.
     * @throws ArrayIndexOutOfBoundsException if {@code offset} or
     *             {@code byteCount} are negative.
     */
    public GCMParameterSpec(int tagLen, byte[] iv, int offset, int byteCount) {
        if (tagLen < 0) {
            throw new IllegalArgumentException("tag should be a non-negative integer");
        }
        if (iv == null) {
            throw new IllegalArgumentException("iv == null");
        }
        if (iv.length - offset < byteCount) {
            throw new IllegalArgumentException("iv.length - offset < byteCount");
        }
        Arrays.checkOffsetAndCount(iv.length, offset, byteCount);
        this.tagLen = tagLen;
        this.iv = new byte[byteCount];
        System.arraycopy(iv, offset, this.iv, 0, byteCount);
    }

    /**
     * Returns the size of the tag in bits.
     */
    public int getTLen() {
        return tagLen;
    }

    /**
     * Returns the Initial Vector (IV) used by this parameter spec.
     */
    public byte[] getIV() {
        byte[] iv = new byte[this.iv.length];
        System.arraycopy(this.iv, 0, iv, 0, iv.length);
        return iv;
    }
}
