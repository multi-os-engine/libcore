/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.security.interfaces;

import junit.framework.TestCase;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;

public class RSAPrivateCrtKeyTest extends TestCase {

    RSAPrivateCrtKey key = null;

    protected void setUp() throws Exception {
        super.setUp();
        /* [XRT] crash
        KeyFactory gen = KeyFactory.getInstance("RSA");
        key = (RSAPrivateCrtKey) gen.generatePrivate(Util.rsaCrtParam);
        */
        fail("[CRASH] Turn off testcase due to crash");
    }

    /**
     * java.security.interfaces.RSAPrivateCrtKey
     * #getCrtCoefficient()
     */
    public void test_getCrtCoefficient() {
        /* [XRT] crash
        assertEquals("invalid CRT coefficient",
                Util.rsaCrtParam.getCrtCoefficient(), key.getCrtCoefficient());
         */
        fail("[CRASH] Turn off testcase due to crash");
    }

    /**
     * java.security.interfaces.RSAPrivateCrtKey
     * #getPrimeExponentP()
     */
    public void test_getPrimeExponentP() {
        /* [XRT] crash
        assertEquals("invalid prime exponent P",
                Util.rsaCrtParam.getPrimeExponentP(), key.getPrimeExponentP());
         */
        fail("[CRASH] Turn off testcase due to crash");
    }

    /**
     * java.security.interfaces.RSAPrivateCrtKey
     * #getPrimeExponentQ()
     */
    public void test_getPrimeExponentQ() {
        /* [XRT] crash
        assertEquals("invalid prime exponent Q",
                Util.rsaCrtParam.getPrimeExponentQ(), key.getPrimeExponentQ());
         */
        fail("[CRASH] Turn off testcase due to crash");
    }

    /**
     * java.security.interfaces.RSAPrivateCrtKey
     * #getPrimeP()
     */
    public void test_getPrimeP() {
        /* [XRT] crash
        assertEquals("invalid prime P",
                Util.rsaCrtParam.getPrimeP(), key.getPrimeP());
         */
        fail("[CRASH] Turn off testcase due to crash");
    }

    /**
     * java.security.interfaces.RSAPrivateCrtKey
     * #getPrimeQ()
     */
    public void test_getPrimeQ() {
        /* [XRT] crash
        assertEquals("invalid prime Q",
                Util.rsaCrtParam.getPrimeQ(), key.getPrimeQ());
         */
        fail("[CRASH] Turn off testcase due to crash");
    }

    /**
     * java.security.interfaces.RSAPrivateCrtKey
     * #getPublicExponent()
     */
    public void test_getPublicExponent() {
        /* [XRT] crash
        assertEquals("invalid public exponent",
                Util.rsaCrtParam.getPublicExponent(), key.getPublicExponent());
         */
        fail("[CRASH] Turn off testcase due to crash");
    }

    protected void tearDown() throws Exception {
        key = null;
        super.tearDown();
    }
}
