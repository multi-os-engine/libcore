/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.jar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The input stream from which the JAR file to be read may be fetched. It is
 * used like the {@code ZipInputStream}.
 *
 * @see ZipInputStream
 */
public class JarInputStream extends ZipInputStream {

    private Manifest manifest;

    private boolean verified = false;

    private JarEntry mEntry;

    private JarEntry jarEntry;

    private boolean isMeta;

    private JarVerifier verifier;

    private OutputStream verStream;

    /**
     * Constructs a new {@code JarInputStream} from an input stream.
     *
     * @param stream
     *            the input stream containing the JAR file.
     * @param verify
     *            if the file should be verified with a {@code JarVerifier}.
     * @throws IOException
     *             If an error occurs reading entries from the input stream.
     * @see ZipInputStream#ZipInputStream(InputStream)
     */
    public JarInputStream(InputStream stream, boolean verify) throws IOException {
        super(stream);
        if (verify) {
            verifier = new JarVerifier("JarInputStream");
        }
        if ((mEntry = getNextJarEntry()) == null) {
            return;
        }
        if (mEntry.getName().equalsIgnoreCase(JarFile.META_DIR)) {
            mEntry = null; // modifies behavior of getNextJarEntry()
            closeEntry();
            mEntry = getNextJarEntry();
        }

        if (mEntry.getName().equalsIgnoreCase(JarFile.MANIFEST_NAME)) {
            mEntry = null;
            manifest = new Manifest(this, verify);
            closeEntry();
            if (verify) {
                verifier.setManifest(manifest);
                verifier.mainAttributesEnd = manifest.getMainAttributesEnd();
            }
        } else {
            // If the manifest isn't the first entry, we will not have enough
            // information to perform verification on entries that precede it.
            //
            // TODO: Should we throw if verify == true in this case ?
            // TODO: We need all meta entries to be placed before the manifest
            // as well.
            verifier = null;
        }
    }

    /**
     * Constructs a new {@code JarInputStream} from an input stream.
     *
     * @param stream
     *            the input stream containing the JAR file.
     * @throws IOException
     *             If an error occurs reading entries from the input stream.
     * @see ZipInputStream#ZipInputStream(InputStream)
     */
    public JarInputStream(InputStream stream) throws IOException {
        this(stream, true);
    }

    /**
     * Returns the {@code Manifest} object associated with this {@code
     * JarInputStream} or {@code null} if no manifest entry exists.
     *
     * @return the MANIFEST specifying the contents of the JAR file.
     */
    public Manifest getManifest() {
        return manifest;
    }

    /**
     * Returns the next {@code JarEntry} contained in this stream or {@code
     * null} if no more entries are present.
     *
     * @return the next JAR entry.
     * @throws IOException
     *             if an error occurs while reading the entry.
     */
    public JarEntry getNextJarEntry() throws IOException {
        return (JarEntry) getNextEntry();
    }

    /**
     * Reads up to {@code byteCount} bytes of decompressed data and stores it in
     * {@code buffer} starting at {@code byteOffset}. Returns the number of uncompressed bytes read.
     *
     * @throws IOException
     *             if an IOException occurs.
     */
    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        if (mEntry != null) {
            return -1;
        }
        int r = super.read(buffer, byteOffset, byteCount);
        // verifier can be null if we've been asked not to verify or if
        // the manifest wasn't found.
        //
        // verStream will be null if we're reading the manifest.
        if (verifier != null && verStream != null && !verified) {
            if (r == -1) {
                // We've hit the end of this stream for the first time, so attempt
                // a verification.
                verified = true;
                if (isMeta) {
                    verifier.addMetaEntry(jarEntry.getName(),
                            ((ByteArrayOutputStream) verStream).toByteArray());
                    try {
                        verifier.readCertificates();
                    } catch (SecurityException e) {
                        verifier = null;
                        throw e;
                    }
                } else {
                    ((JarVerifier.VerifierEntry) verStream).verify();
                }
            } else {
                verStream.write(buffer, byteOffset, r);
            }
        }
        return r;
    }

    /**
     * Returns the next {@code ZipEntry} contained in this stream or {@code
     * null} if no more entries are present.
     *
     * @return the next extracted ZIP entry.
     * @throws IOException
     *             if an error occurs while reading the entry.
     */
    @Override
    public ZipEntry getNextEntry() throws IOException {
        if (mEntry != null) {
            jarEntry = mEntry;
            mEntry = null;
        } else {
            final JarEntry jarEntry = (JarEntry) super.getNextEntry();
            if (jarEntry == null) {
                return null;
            }

            if (verifier != null) {
                isMeta = jarEntry.getName().toUpperCase(Locale.US).startsWith(JarFile.META_DIR);
                if (isMeta) {
                    final int entrySize = (int) jarEntry.getSize();
                    verStream = new ByteArrayOutputStream(entrySize > 0 ? entrySize : 8192);
                } else {
                    verStream = verifier.initEntry(jarEntry.getName());
                }
            }
        }

        verified = false;
        return jarEntry;
    }

    @Override
    protected ZipEntry createZipEntry(String name) {
        JarEntry entry = new JarEntry(name);
        if (manifest != null) {
            entry.setAttributes(manifest.getAttributes(name));
        }
        return entry;
    }
}
