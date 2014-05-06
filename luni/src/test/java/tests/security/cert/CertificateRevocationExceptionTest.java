/*
 * Copyright 2014 The Android Open Source Project
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

package tests.security.cert;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.cert.CRLReason;
import java.security.cert.CertificateRevokedException;
import java.security.cert.Extension;
import java.util.Date;
import java.util.HashMap;

import javax.security.auth.x500.X500Principal;

import junit.framework.TestCase;

/**
 *
 */
public class CertificateRevocationExceptionTest extends TestCase implements SerializableAssert {
    private static HashMap<String, Extension> CERT_EXTENSIONS = new HashMap<String, Extension>();
    static {
        // REASON_CODE
        CERT_EXTENSIONS.put("2.5.29.21", new Extension() {
            @Override
            public String getId() {
                return "2.5.29.21";
            }

            @Override
            public boolean isCritical() {
                return false;
            }

            @Override
            public byte[] getValue() {
                return new byte[] {4, 3, 10, 1, 5};
            }

            @Override
            public void encode(OutputStream out) throws IOException {
                throw new UnsupportedOperationException();
            }
        });
    }

    private static CertificateRevokedException CERT_REVOCATION_EXCEPTION
            = new CertificateRevokedException(
                    new Date(108, 0, 1, 14, 34, 11),
                    CRLReason.CESSATION_OF_OPERATION,
                    new X500Principal("CN=test1"),
                    CERT_EXTENSIONS);

    /**
     * serialization/deserialization compatibility.
     */
    public void testSerializationCertificateRevokedExceptionSelf() throws Exception {
        SerializationTest.verifySelf(CERT_REVOCATION_EXCEPTION, this);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCertificateRevokedExceptionCompatability() throws Exception {
        // create test file (once)
        // SerializationTest.createGoldenFile("/sdcard", this, CERT_REVOCATION_EXCEPTION);
        SerializationTest.verifyGolden(this, CERT_REVOCATION_EXCEPTION);
    }

    @Override
    public void assertDeserialized(Serializable initial, Serializable deserialized) {
        assertTrue(initial instanceof CertificateRevokedException);
        assertTrue(deserialized instanceof CertificateRevokedException);

        CertificateRevokedException expected = (CertificateRevokedException) initial;
        CertificateRevokedException actual = (CertificateRevokedException) deserialized;

        assertEquals(expected.getInvalidityDate(), actual.getInvalidityDate());
        assertEquals(expected.getRevocationDate(), actual.getRevocationDate());
        assertEquals(expected.getRevocationReason(), expected.getRevocationReason());

        assertEquals(expected.getExtensions().size(), actual.getExtensions().size());
        assertEquals(expected.getExtensions().keySet(), actual.getExtensions().keySet());
    }
}
