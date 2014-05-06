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
public class CertificateRevocationExceptionTest extends TestCase {

    private static HashMap<String, Extension> CERT_EXTENSIONS = new HashMap<String, Extension>();

    private static CertificateRevokedException CERT_REVOCATION_EXCEPTION
            = new CertificateRevokedException(
                    new Date(2148, 0, 1, 14, 34, 11),
                    CRLReason.CESSATION_OF_OPERATION,
                    new X500Principal("CN=test1"),
                    CERT_EXTENSIONS);

    /**
     * serialization/deserialization compatibility.
     */
    public void testSerializationCertificateRevokedExceptionSelf() throws Exception {
        SerializationTest.verifySelf(CERT_REVOCATION_EXCEPTION);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCertificateRevokedExceptionCompatability() throws Exception {
        // create test file (once)
        // SerializationTest.createGoldenFile("/sdcard", this, CERT_REVOCATION_EXCEPTION);
        SerializationTest.verifyGolden(this, CERT_REVOCATION_EXCEPTION);
    }
}
