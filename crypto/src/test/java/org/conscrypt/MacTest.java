package org.conscrypt;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.TestCase;

public class MacTest extends TestCase {
    public void test_getInstance_OpenSSL_ENGINE() throws Exception {
        final String secret = "-HMAC-test1";
        final byte[] testString = "testing123".getBytes();

        Provider p = Security.getProvider(OpenSSLProvider.PROVIDER_NAME);
        NativeCryptoTest.loadTestEngine();
        OpenSSLEngine engine = OpenSSLEngine.getInstance(NativeCryptoTest.TEST_ENGINE_ID);

        /*
         * The "-HMAC-" prefix is a special prefix recognized by
         * test_openssl_engine.cpp
         */
        SecretKey key1 = engine.getSecretKeyById(secret, "HmacSHA256");
        SecretKey key1dupe = engine.getSecretKeyById(secret, "HmacSHA256");

        /* Non-ENGINE-based SecretKey */
        SecretKey key2 = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

        /* The one that is ENGINE-based can't be equal to a non-ENGINE one. */
        assertFalse(key1.equals(key2));
        assertEquals(key1, key1dupe);
        assertNull(key1.getFormat());
        assertNull(key1.getEncoded());
        assertEquals("RAW", key2.getFormat());
        assertEquals(Arrays.toString(secret.getBytes()), Arrays.toString(key2.getEncoded()));

        Mac mac1 = Mac.getInstance("HmacSHA256", p);
        mac1.init(key1);
        mac1.update(testString);
        byte[] output1 = mac1.doFinal();
        assertEquals(mac1.getMacLength(), output1.length);

        Mac mac2 = Mac.getInstance("HmacSHA256", p);
        mac2.init(key2);
        mac2.update(testString);
        byte[] output2 = mac2.doFinal();

        assertEquals(Arrays.toString(output2), Arrays.toString(output1));
    }
}
