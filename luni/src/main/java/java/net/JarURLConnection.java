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

package java.net;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import libcore.net.UriCodec;

/**
 * This class establishes a connection to a {@code jar:} URL using the {@code
 * JAR} protocol. A {@code JarURLConnection} instance can refer to either a JAR
 * archive file or to an entry of such a file. {@code jar:} URLs are specified
 * as follows: <i>jar:{archive-url}!/{entry}</i> where "!/" is called a
 * separator. This separator is important to determine if an archive or an entry
 * of an archive is referred.
 * <p>
 * Examples:
 * <ul>
 * <li>Archive: {@code jar:http://www.example.com/applets/archive.jar!/}</li>
 * <li>File Entry: {@code
 * jar:http://www.example.com/applets/archive.jar!/test.class}</li>
 * <li>Directory Entry: {@code
 * jar:http://www.example.com/applets/archive.jar!/applets/}</li>
 * </ul>
 * <p>
 * This class has different behavior depending on the {@link #getUseCaches()}
 * setting. When caching is enabled the underlying {@code JarFile} returned from
 * {@link #getJarFile()} is cached and shared between connections. It must not be
 * closed by the caller. Disabling caching using {@link #setUseCaches(boolean)}
 * will prevent sharing but in the absence of a
 * {@code JarURLConnection.disconnect()} method the only way to close the
 * underlying {@code JarFile} is to call {@code getJarFile().close()}
 * or {@code getInputStream().close()}. For uncached connections, closing the
 * {@code JarFile} can prevent various other {@link URLConnection} methods from
 * working.
 */
public abstract class JarURLConnection extends URLConnection {

    /**
     * The location part of the represented URL.
     */
    protected URLConnection jarFileURLConnection;

    private String entryName;

    private URL fileURL;

    // the file component of the URL
    private String file;

    /**
     * Constructs an instance of {@code JarURLConnection} that refers to the
     * specified URL.
     *
     * @param url
     *            the URL that contains the location to connect to.
     * @throws MalformedURLException
     *             if an invalid URL has been entered.
     */
    protected JarURLConnection(URL url) throws MalformedURLException {
        super(url);
        file = decode(url.getFile());

        int sepIdx;
        if ((sepIdx = file.indexOf("!/")) < 0) {
            throw new MalformedURLException();
        }
        fileURL = new URL(file.substring(0, sepIdx));
        sepIdx += 2;
        if (file.length() == sepIdx) {
            return;
        }
        entryName = file.substring(sepIdx, file.length());
        if (url.getRef() != null) {
            entryName += "#" + url.getRef();
        }
    }

    /**
     * Returns all attributes of the {@code JarEntry} referenced by this {@code
     * JarURLConnection}.
     *
     * @return the attributes of the referenced {@code JarEntry}.
     * @throws IOException
     *                if an I/O exception occurs while retrieving the
     *                JAR-entries.
     */
    public Attributes getAttributes() throws java.io.IOException {
        JarEntry jEntry = getJarEntry();
        return (jEntry == null) ? null : jEntry.getAttributes();
    }

    /**
     * Returns all certificates of the {@code JarEntry} referenced by this
     * {@code JarURLConnection} instance. This method will return {@code null}
     * until the {@code InputStream} has been completely verified.
     *
     * @return the certificates of the {@code JarEntry} as an array.
     * @throws IOException
     *                if there is an I/O exception occurs while getting the
     *                {@code JarEntry}.
     */
    public Certificate[] getCertificates() throws java.io.IOException {
        JarEntry jEntry = getJarEntry();
        if (jEntry == null) {
            return null;
        }

        return jEntry.getCertificates();
    }

    /**
     * Gets the name of the entry referenced by this {@code JarURLConnection}.
     * The return value will be {@code null} if this instance refers to a JAR
     * file rather than an JAR file entry.
     *
     * @return the {@code JarEntry} name this instance refers to.
     */
    public String getEntryName() {
        return entryName;
    }

    /**
     * Gets the {@code JarEntry} object of the entry referenced by this {@code
     * JarURLConnection}.
     *
     * @return the referenced {@code JarEntry} object or {@code null} if no
     *         entry name is specified.
     * @throws IOException
     *             if an error occurs while getting the file or file-entry.
     */
    public JarEntry getJarEntry() throws IOException {
        if (!connected) {
            connect();
        }
        if (entryName == null) {
            return null;
        }
        // The entry must exist since the connect succeeded
        return getJarFile().getJarEntry(entryName);
    }

    /**
     * Gets the manifest file associated with this JAR-URL.
     *
     * @return the manifest of the referenced JAR-file.
     * @throws IOException
     *             if an error occurs while getting the manifest file.
     */
    public Manifest getManifest() throws java.io.IOException {
        return (Manifest)getJarFile().getManifest().clone();
    }

    /**
     * Gets the {@code JarFile} object referenced by this {@code
     * JarURLConnection}.
     *
     * @return the referenced JarFile object.
     * @throws IOException
     *                if an I/O exception occurs while retrieving the JAR-file.
     */
    public abstract JarFile getJarFile() throws java.io.IOException;

    /**
     * Gets the URL to the JAR-file referenced by this {@code JarURLConnection}.
     *
     * @return the URL to the JAR-file or {@code null} if there was an error
     *         retrieving the URL.
     */
    public URL getJarFileURL() {
        return fileURL;
    }

    /**
     * Gets all attributes of the manifest file referenced by this {@code
     * JarURLConnection}. If this instance refers to a JAR-file rather than a
     * JAR-file entry, {@code null} will be returned.
     *
     * @return the attributes of the manifest file or {@code null}.
     * @throws IOException
     *                if an I/O exception occurs while retrieving the {@code
     *                JarFile}.
     */
    public Attributes getMainAttributes() throws java.io.IOException {
        Manifest m = getJarFile().getManifest();
        return (m == null) ? null : m.getMainAttributes();
    }

    private static String decode(String encoded) throws MalformedURLException {
        try {
            // "+" means "+" in URLs. i.e. like RFC 3986, not like
            // MIME application/x-www-form-urlencoded
            final boolean convertPlus = false;
            return UriCodec.decode(
                    encoded, convertPlus, StandardCharsets.UTF_8, true /* throwOnFailure */);
        } catch (IllegalArgumentException e) {
            throw new MalformedURLException("Unable to decode URL", e);
        }
    }

}
