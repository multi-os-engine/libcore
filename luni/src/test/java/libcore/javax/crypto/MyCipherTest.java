/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.javax.crypto;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class MyCipherTest extends SimpleBenchmark {


    @Param({"BC", "BC-avoid-openssl"}) String provider;

    public void setUp() {
        Security.addProvider(new minimumpatch.com.android.org.bouncycastle.jce.provider.BouncyCastleProvider());
        Security.addProvider(new avoidopenssl.com.android.org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public void timeGetProviderVersion(int rep) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1", provider);

        System.out.println("Provider for the factory:" + factory.getProvider().getInfo());

        for (int i = 0; i < rep; i++) {
            SecretKey skey = runCode(factory, provider);
        }
    }

    private SecretKey runCode(SecretKeyFactory factory, String provider) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec("mypassword".toCharArray(), "mysalt".getBytes("UTF-8"), 10000, 256);
        SecretKey ret;
        try {
            ret = factory.generateSecret(keySpec);
        } catch (InvalidKeySpecException e) {
            return new SecretKey() {
                @Override
                public String getAlgorithm() {
                    return null;
                }

                @Override
                public String getFormat() {
                    return null;
                }

                @Override
                public byte[] getEncoded() {
                    return new byte[0];
                }
            };
        }
        return ret;
    }
}
