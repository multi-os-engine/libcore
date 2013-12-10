/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.net.ssl;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import javax.net.SocketFactory;

/**
 * The abstract factory implementation to create {@code SSLSocket}s.
 *
 * <h3>Default instance</h3>
 * <p>The default instance of the factory can be obtained via
 * {@link #getDefault()}. It has the following configuration:
 *
 * <table>
 *     <thead>
 *         <tr>
 *             <th>Protocol</th>
 *             <th>Supported (API Levels)</th>
 *             <th>Enabled by default (API Levels)</th>
 *         </tr>
 *     </thead>
 *
 *     <tbody>
 *         <tr>
 *             <td>SSLv3</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>TLSv1</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>TLSv1.1</td>
 *             <td>16+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLSv1.2</td>
 *             <td>16+</td>
 *             <td></td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>From API Level 9 onwards, standard cipher suites name have been used as listed in the table
 * below. Prior to API Level 9, non-standard (OpenSSL) cipher suite names had been used (see the
 * table following this table).
 * <table>
 *     <thead>
 *         <tr>
 *             <th>Cipher suite</th>
 *             <th>Supported (API Levels)</th>
 *             <th>Enabled by default (API Levels)</th>
 *         </tr>
 *     </thead>
 *
 *     <tbody>
 *         <tr>
 *             <td>SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_DSS_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_RSA_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_EXPORT_WITH_RC4_40_MD5</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_WITH_RC4_128_MD5</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_EXPORT_WITH_RC4_40_MD5</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_NULL_MD5</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_NULL_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_RC4_128_MD5</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_RC4_128_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_NULL_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_RC4_128_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_NULL_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_RC4_128_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_NULL_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_RC4_128_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_NULL_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_RC4_128_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_anon_WITH_AES_128_CBC_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_anon_WITH_AES_256_CBC_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_anon_WITH_NULL_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_EMPTY_RENEGOTIATION_INFO_SCSV</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_anon_WITH_RC4_128_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>11+</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>Platforms with API Levels 1 to 8 use OpenSSL names for cipher suites. The table below lists
 * these OpenSSL cipher suite names as well as the corresponding standard names which are used on
 * platforms with API Levels 9 and above.
 * <table>
 *     <thead>
 *         <tr>
 *             <th>OpenSSL cipher suite</th>
 *             <th>Standard cipher suite</th>
 *             <th>Supported (API Levels)</th>
 *             <th>Enabled by default (API Levels)</th>
 *         </tr>
 *     </thead>
 *
 *     <tbody>
 *         <tr>
 *             <td>AES128-SHA</td>
 *             <td>TLS_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>AES256-SHA</td>
 *             <td>TLS_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-8, 11+</td>
 *         </tr>
 *         <tr>
 *             <td>DES-CBC-MD5</td>
 *             <td>SSL_CK_DES_64_CBC_WITH_MD5</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>DES-CBC-SHA</td>
 *             <td>SSL_RSA_WITH_DES_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>DES-CBC3-MD5</td>
 *             <td>SSL_CK_DES_192_EDE3_CBC_WITH_MD5</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>DES-CBC3-SHA</td>
 *             <td>SSL_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>DHE-DSS-AES128-SHA</td>
 *             <td>TLS_DHE_DSS_WITH_AES_128_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>DHE-DSS-AES256-SHA</td>
 *             <td>TLS_DHE_DSS_WITH_AES_256_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-8, 11+</td>
 *         </tr>
 *         <tr>
 *             <td>DHE-RSA-AES128-SHA</td>
 *             <td>TLS_DHE_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>DHE-RSA-AES256-SHA</td>
 *             <td>TLS_DHE_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-8, 11+</td>
 *         </tr>
 *         <tr>
 *             <td>EDH-DSS-DES-CBC-SHA</td>
 *             <td>SSL_DHE_DSS_WITH_DES_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EDH-DSS-DES-CBC3-SHA</td>
 *             <td>SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>EDH-RSA-DES-CBC-SHA</td>
 *             <td>SSL_DHE_RSA_WITH_DES_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EDH-RSA-DES-CBC3-SHA</td>
 *             <td>SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>EXP-DES-CBC-SHA</td>
 *             <td>SSL_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EXP-EDH-DSS-DES-CBC-SHA</td>
 *             <td>SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EXP-EDH-RSA-DES-CBC-SHA</td>
 *             <td>SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EXP-RC2-CBC-MD5</td>
 *             <td>SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>EXP-RC4-MD5</td>
 *             <td>SSL_RSA_EXPORT_WITH_RC4_40_MD5</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>RC2-CBC-MD5</td>
 *             <td>SSL_CK_RC2_128_CBC_WITH_MD5</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>RC4-MD5</td>
 *             <td>SSL_RSA_WITH_RC4_128_MD5</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>RC4-SHA</td>
 *             <td>SSL_RSA_WITH_RC4_128_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *     </tbody>
 * </table>
 */
public abstract class SSLSocketFactory extends SocketFactory {
    // FIXME EXPORT CONTROL

    // The default SSL socket factory
    private static SocketFactory defaultSocketFactory;

    private static String defaultName;

    /**
     * Returns the default {@code SSLSocketFactory} instance. The default is
     * defined by the security property {@code 'ssl.SocketFactory.provider'}.
     *
     * @return the default ssl socket factory instance.
     */
    public static synchronized SocketFactory getDefault() {
        if (defaultSocketFactory != null) {
            return defaultSocketFactory;
        }
        if (defaultName == null) {
            defaultName = Security.getProperty("ssl.SocketFactory.provider");
            if (defaultName != null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) {
                    cl = ClassLoader.getSystemClassLoader();
                }
                try {
                    final Class<?> sfc = Class.forName(defaultName, true, cl);
                    defaultSocketFactory = (SocketFactory) sfc.newInstance();
                } catch (Exception e) {
                    System.logE("Problem creating " + defaultName, e);
                }
            }
        }

        if (defaultSocketFactory == null) {
            SSLContext context;
            try {
                context = SSLContext.getDefault();
            } catch (NoSuchAlgorithmException e) {
                context = null;
            }
            if (context != null) {
                defaultSocketFactory = context.getSocketFactory();
            }
        }
        if (defaultSocketFactory == null) {
            // Use internal implementation
            defaultSocketFactory = new DefaultSSLSocketFactory("No SSLSocketFactory installed");
        }
        return defaultSocketFactory;
    }

    /**
     * Creates a new {@code SSLSocketFactory}.
     */
    public SSLSocketFactory() {
    }

    /**
     * Returns the names of the cipher suites that are enabled by default.
     *
     * @return the names of the cipher suites that are enabled by default.
     */
    public abstract String[] getDefaultCipherSuites();

    /**
     * Returns the names of the cipher suites that are supported and could be
     * enabled for an SSL connection.
     *
     * @return the names of the cipher suites that are supported.
     */
    public abstract String[] getSupportedCipherSuites();

    /**
     * Creates an {@code SSLSocket} over the specified socket that is connected
     * to the specified host at the specified port.
     *
     * @param s
     *            the socket.
     * @param host
     *            the host.
     * @param port
     *            the port number.
     * @param autoClose
     *            {@code true} if socket {@code s} should be closed when the
     *            created socket is closed, {@code false} if the socket
     *            {@code s} should be left open.
     * @return the creates ssl socket.
     * @throws IOException
     *             if creating the socket fails.
     * @throws java.net.UnknownHostException
     *             if the host is unknown.
     */
    public abstract Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException;
}
