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

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.util.Collection;

public class OpenSSLX509CertificateFactory extends CertificateFactorySpi {

    @Override
    public Certificate engineGenerateCertificate(InputStream inStream) throws CertificateException {
        final PushbackInputStream pbis = new PushbackInputStream(inStream);
        try {
            int firstByte = pbis.read();
            pbis.unread(firstByte);

            if (firstByte == '-') {
                return OpenSSLX509Certificate.fromPemInputStream(pbis);
            }

            return OpenSSLX509Certificate.fromDerInputStream(pbis);
        } catch (IOException e) {
            throw new CertificateException(e);
        }
    }

    @Override
    public Collection<? extends java.security.cert.Certificate> engineGenerateCertificates(
            InputStream inStream)
            throws CertificateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CRL engineGenerateCRL(InputStream inStream) throws CRLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<? extends CRL> engineGenerateCRLs(InputStream inStream) throws CRLException {
        // TODO Auto-generated method stub
        return null;
    }

}
