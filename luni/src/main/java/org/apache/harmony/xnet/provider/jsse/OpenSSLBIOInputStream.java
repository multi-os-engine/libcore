/*
 * Copyright (C) 2012 The Android Open Source Project
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

package org.apache.harmony.xnet.provider.jsse;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OpenSSLBIOInputStream extends FilterInputStream {
    public OpenSSLBIOInputStream(InputStream is) {
        super(is);
    }

    public int readLine(byte[] buffer) throws IOException {
        if (buffer == null || buffer.length == 0) {
            return 0;
        }

        /*
         * OpenSSL complains if your Base64 encoding is
         * >= 80 columns yet it usually passes in a larger
         * buffer, so just trim this line down.
         */
        final int maxOffset;
        if (buffer.length <= 63) {
            maxOffset = buffer.length - 1;
        } else {
            maxOffset = 63;
        }

        int offset = 0;
        int inputByte = read();
        while (offset < maxOffset && inputByte != '\n' && inputByte != -1) {
            buffer[offset++] = (byte) inputByte;
            inputByte = read();
        }

        if (inputByte == '\n') {
            buffer[offset++] = '\n';
        }

        return offset;
    }
}
