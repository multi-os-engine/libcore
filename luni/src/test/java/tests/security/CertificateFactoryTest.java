/*
 * Copyright (C) 2009 The Android Open Source Project
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
package tests.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import junit.framework.TestCase;

public abstract class CertificateFactoryTest extends TestCase {

    private final String algorithmName;
    private final byte[] certificateData;


    public CertificateFactoryTest(String algorithmName, byte[] certificateData) {
        this.algorithmName = algorithmName;
        this.certificateData = certificateData;
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCertificateFactory() throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(
                algorithmName);

        Certificate certificate = certificateFactory.generateCertificate(
                new ByteArrayInputStream(certificateData));
        assertNotNull(certificate);
    }

    public void testCertificateFactory_InputStream_Offset_Correct() throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(algorithmName);

        byte[] doubleCertificateData = new byte[certificateData.length * 2];
        MeasuredInputStream certStream = new MeasuredInputStream(new ByteArrayInputStream(doubleCertificateData));
        Certificate certificate = certificateFactory.generateCertificate(certStream);
        assertNotNull(certificate);
        assertEquals(certificateData.length, certStream.getCount());
    }

    /**
     * Proxy that counts the number of bytes read from an InputStream.
     */
    private static class MeasuredInputStream extends InputStream {
        private long mCount = 0;

        private InputStream mStream;

        public MeasuredInputStream(InputStream is) {
            mStream = is;
        }

        public long getCount() {
            return mCount;
        }

        @Override
        public int read() throws IOException {
            int nextByte = mStream.read();
            mCount++;
            return nextByte;
        }

        @Override
        public int read(byte[] buffer) throws IOException {
            int count = mStream.read(buffer);
            mCount += count;
            return count;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int count = mStream.read(buffer, offset, length);
            mCount += count;
            return count;
        }

        @Override
        public long skip(long byteCount) throws IOException {
            long count = mStream.skip(byteCount);
            mCount += count;
            return count;
        }

        @Override
        public int available() throws IOException {
            return mStream.available();
        }

        @Override
        public void close() throws IOException {
            mStream.close();
        }

        @Override
        public void mark(int readlimit) {
            mStream.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return mStream.markSupported();
        }

        @Override
        public synchronized void reset() throws IOException {
            mStream.reset();
        }
    }
}
