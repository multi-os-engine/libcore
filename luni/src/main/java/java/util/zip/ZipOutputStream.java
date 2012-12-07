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

package java.util.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charsets;
import java.util.Arrays;
import java.util.HashSet;
import libcore.util.EmptyArray;

/**
 * This class provides an implementation of {@code FilterOutputStream} that
 * compresses data entries into a <i>ZIP-archive</i> output stream.
 * <p>
 * {@code ZipOutputStream} is used to write {@code ZipEntries} to the underlying
 * stream. Output from {@code ZipOutputStream} conforms to the {@code ZipFile}
 * file format.
 * <p>
 * While {@code DeflaterOutputStream} can write a compressed <i>ZIP-archive</i>
 * entry, this extension can write uncompressed entries as well. In this case
 * special rules apply, for this purpose refer to the <a
 * href="http://www.pkware.com/documents/casestudies/APPNOTE.TXT">file format
 * specification</a>.
 *
 * <h3>Example</h3>
 * <p>Using {@code ZipOutputStream} is a little more complicated than {@link GZIPOutputStream}
 * because ZIP archives are containers that can contain multiple files. This code creates a ZIP
 * archive containing several files, similar to the {@code zip(1)} utility.
 * <pre>
 * OutputStream os = ...
 * ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
 * try {
 *     for (int i = 0; i < fileCount; ++i) {
 *         String filename = ...
 *         byte[] bytes = ...
 *         ZipEntry entry = new ZipEntry(filename);
 *         zos.putNextEntry(entry);
 *         zos.write(bytes);
 *         zos.closeEntry();
 *     }
 * } finally {
 *     zos.close();
 * }
 * </pre>
 *
 * @see ZipEntry
 * @see ZipFile
 */
public class ZipOutputStream extends DeflaterOutputStream implements ZipConstants {

    /**
     * Indicates deflated entries.
     */
    public static final int DEFLATED = 8;

    /**
     * Indicates uncompressed entries.
     */
    public static final int STORED = 0;

    private static final int ZIPLocalHeaderVersionNeeded = 20;

    private byte[] commentBytes = EmptyArray.BYTE;

    private final HashSet<String> entries = new HashSet<String>();

    private int defaultCompressionMethod = DEFLATED;

    private int compressionLevel = Deflater.DEFAULT_COMPRESSION;

    private ByteArrayOutputStream cDir = new ByteArrayOutputStream();

    private ZipEntry currentEntry;

    private final CRC32 crc = new CRC32();

    private int offset = 0, curOffset = 0, nameLength;

    private byte[] nameBytes;

    /**
     * Constructs a new {@code ZipOutputStream} that writes a zip file
     * to the given {@code OutputStream}.
     */
    public ZipOutputStream(OutputStream os) {
        super(os, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
    }

    /**
     * Closes the current {@code ZipEntry}, if any, and the underlying output
     * stream. If the stream is already closed this method does nothing.
     *
     * @throws IOException
     *             If an error occurs closing the stream.
     */
    @Override
    public void close() throws IOException {
        // don't call super.close() because that calls finish() conditionally
        if (out != null) {
            finish();
            def.end();
            out.close();
            out = null;
        }
    }

    /**
     * Closes the current {@code ZipEntry}. Any entry terminal data is written
     * to the underlying stream.
     *
     * @throws IOException
     *             If an error occurs closing the entry.
     */
    public void closeEntry() throws IOException {
        checkOpen();
        if (currentEntry == null) {
            return;
        }
        if (currentEntry.getMethod() == DEFLATED) {
            super.finish();
        }

        // Verify values for STORED types
        if (currentEntry.getMethod() == STORED) {
            if (crc.getValue() != currentEntry.crc) {
                throw new ZipException("CRC mismatch");
            }
            if (currentEntry.size != crc.tbytes) {
                throw new ZipException("Size mismatch");
            }
        }
        curOffset = LOCHDR;

        // Write the DataDescriptor
        if (currentEntry.getMethod() != STORED) {
            curOffset += EXTHDR;
            writeLong(out, EXTSIG);
            writeLong(out, currentEntry.crc = crc.getValue());
            writeLong(out, currentEntry.compressedSize = def.getTotalOut());
            writeLong(out, currentEntry.size = def.getTotalIn());
        }
        // Update the CentralDirectory
        // http://www.pkware.com/documents/casestudies/APPNOTE.TXT
        int flags = currentEntry.getMethod() == STORED ? 0 : ZipFile.GPBF_DATA_DESCRIPTOR_FLAG;
        // Since gingerbread, we always set the UTF-8 flag on individual files.
        // Some tools insist that the central directory also have the UTF-8 flag.
        // http://code.google.com/p/android/issues/detail?id=20214
        flags |= ZipFile.GPBF_UTF8_FLAG;
        writeLong(cDir, CENSIG);
        writeShort(cDir, ZIPLocalHeaderVersionNeeded); // Version created
        writeShort(cDir, ZIPLocalHeaderVersionNeeded); // Version to extract
        writeShort(cDir, flags);
        writeShort(cDir, currentEntry.getMethod());
        writeShort(cDir, currentEntry.time);
        writeShort(cDir, currentEntry.modDate);
        writeLong(cDir, crc.getValue());
        if (currentEntry.getMethod() == DEFLATED) {
            curOffset += writeLong(cDir, def.getTotalOut());
            writeLong(cDir, def.getTotalIn());
        } else {
            curOffset += writeLong(cDir, crc.tbytes);
            writeLong(cDir, crc.tbytes);
        }
        curOffset += writeShort(cDir, nameLength);
        if (currentEntry.extra != null) {
            curOffset += writeShort(cDir, currentEntry.extra.length);
        } else {
            writeShort(cDir, 0);
        }

        String comment = currentEntry.getComment();
        byte[] commentBytes = EmptyArray.BYTE;
        if (comment != null) {
            commentBytes = comment.getBytes(Charsets.UTF_8);
        }
        writeShort(cDir, commentBytes.length); // Comment length.
        writeShort(cDir, 0); // Disk Start
        writeShort(cDir, 0); // Internal File Attributes
        writeLong(cDir, 0); // External File Attributes
        writeLong(cDir, offset);
        cDir.write(nameBytes);
        nameBytes = null;
        if (currentEntry.extra != null) {
            cDir.write(currentEntry.extra);
        }
        offset += curOffset;
        if (commentBytes.length > 0) {
            cDir.write(commentBytes);
        }
        currentEntry = null;
        crc.reset();
        def.reset();
        done = false;
    }

    /**
     * Indicates that all entries have been written to the stream. Any terminal
     * information is written to the underlying stream.
     *
     * @throws IOException
     *             if an error occurs while terminating the stream.
     */
    @Override
    public void finish() throws IOException {
        // TODO: is there a bug here? why not checkOpen?
        if (out == null) {
            throw new IOException("Stream is closed");
        }
        if (cDir == null) {
            return;
        }
        if (entries.isEmpty()) {
            throw new ZipException("No entries");
        }
        if (currentEntry != null) {
            closeEntry();
        }
        int cdirSize = cDir.size();
        // Write Central Dir End
        writeLong(cDir, ENDSIG);
        writeShort(cDir, 0); // Disk Number
        writeShort(cDir, 0); // Start Disk
        writeShort(cDir, entries.size()); // Number of entries
        writeShort(cDir, entries.size()); // Number of entries
        writeLong(cDir, cdirSize); // Size of central dir
        writeLong(cDir, offset); // Offset of central dir
        writeShort(cDir, commentBytes.length);
        if (commentBytes.length > 0) {
            cDir.write(commentBytes);
        }
        // Write the central directory.
        cDir.writeTo(out);
        cDir = null;
    }

    /**
     * Writes entry information to the underlying stream. Data associated with
     * the entry can then be written using {@code write()}. After data is
     * written {@code closeEntry()} must be called to complete the writing of
     * the entry to the underlying stream.
     *
     * @param ze
     *            the {@code ZipEntry} to store.
     * @throws IOException
     *             If an error occurs storing the entry.
     * @see #write
     */
    public void putNextEntry(ZipEntry ze) throws IOException {
        if (currentEntry != null) {
            closeEntry();
        }

        // Did this ZipEntry specify a method, or should we use the default?
        int method = ze.getMethod();
        if (method == -1) {
            method = defaultCompressionMethod;
        }

        // If the method is STORED, check that the ZipEntry was configured appropriately.
        if (method == STORED) {
            if (ze.getCompressedSize() == -1) {
                ze.setCompressedSize(ze.getSize());
            } else if (ze.getSize() == -1) {
                ze.setSize(ze.getCompressedSize());
            }
            if (ze.getCrc() == -1) {
                throw new ZipException("STORED entry missing CRC");
            }
            if (ze.getSize() == -1) {
                throw new ZipException("STORED entry missing size");
            }
            if (ze.size != ze.compressedSize) {
                throw new ZipException("STORED entry size/compressed size mismatch");
            }
        }

        checkOpen();

        if (entries.contains(ze.name)) {
            throw new ZipException("Entry already exists: " + ze.name);
        }
        if (entries.size() == 64*1024-1) {
            // TODO: support Zip64.
            throw new ZipException("Too many entries for the zip file format's 16-bit entry count");
        }
        nameBytes = ze.name.getBytes(Charsets.UTF_8);
        nameLength = nameBytes.length;
        if (nameLength > 0xffff) {
            throw new IllegalArgumentException("Name too long: " + nameLength + " UTF-8 bytes");
        }

        def.setLevel(compressionLevel);
        ze.setMethod(method);

        currentEntry = ze;
        entries.add(currentEntry.name);

        // Local file header.
        // http://www.pkware.com/documents/casestudies/APPNOTE.TXT
        int flags = (method == STORED) ? 0 : ZipFile.GPBF_DATA_DESCRIPTOR_FLAG;
        // Java always outputs UTF-8 filenames. (Before Java 7, the RI didn't set this flag and used
        // modified UTF-8. From Java 7, it sets this flag and uses normal UTF-8.)
        flags |= ZipFile.GPBF_UTF8_FLAG;
        writeLong(out, LOCSIG); // Entry header
        writeShort(out, ZIPLocalHeaderVersionNeeded); // Extraction version
        writeShort(out, flags);
        writeShort(out, currentEntry.getMethod());
        if (currentEntry.getTime() == -1) {
            currentEntry.setTime(System.currentTimeMillis());
        }
        writeShort(out, currentEntry.time);
        writeShort(out, currentEntry.modDate);

        if (currentEntry.getMethod() == STORED) {
            if (currentEntry.size == -1) {
                currentEntry.size = currentEntry.compressedSize;
            } else if (currentEntry.compressedSize == -1) {
                currentEntry.compressedSize = currentEntry.size;
            }
            writeLong(out, currentEntry.crc);
            writeLong(out, currentEntry.size);
            writeLong(out, currentEntry.size);
        } else {
            writeLong(out, 0);
            writeLong(out, 0);
            writeLong(out, 0);
        }
        writeShort(out, nameLength);
        if (currentEntry.extra != null) {
            writeShort(out, currentEntry.extra.length);
        } else {
            writeShort(out, 0);
        }
        out.write(nameBytes);
        if (currentEntry.extra != null) {
            out.write(currentEntry.extra);
        }
    }

    /**
     * Sets the comment associated with the file being written.
     * @throws IllegalArgumentException if the comment is longer than 64 KiB.
     */
    public void setComment(String comment) {
        if (comment == null) {
            this.commentBytes = null;
            return;
        }

        byte[] newCommentBytes = comment.getBytes(Charsets.UTF_8);
        if (newCommentBytes.length > 0xffff) {
            throw new IllegalArgumentException("Comment too long: " + newCommentBytes.length + " bytes");
        }
        this.commentBytes = newCommentBytes;
    }

    /**
     * Sets the compression level to be used for writing entry data. This level
     * may be set on a per entry basis. The level must have a value between -1
     * and 8 according to the {@code Deflater} compression level bounds.
     *
     * @param level
     *            the compression level (ranging from -1 to 8).
     * @see Deflater
     */
    public void setLevel(int level) {
        if (level < Deflater.DEFAULT_COMPRESSION || level > Deflater.BEST_COMPRESSION) {
            throw new IllegalArgumentException("Bad level: " + level);
        }
        compressionLevel = level;
    }

    /**
     * Sets the compression method to be used when compressing entry data.
     * method must be one of {@code STORED} (for no compression) or {@code
     * DEFLATED}.
     *
     * @param method
     *            the compression method to use.
     */
    public void setMethod(int method) {
        if (method != STORED && method != DEFLATED) {
            throw new IllegalArgumentException("Bad method: " + method);
        }
        defaultCompressionMethod = method;
    }

    private long writeLong(OutputStream os, long i) throws IOException {
        // Write out the long value as an unsigned int
        os.write((int) (i & 0xFF));
        os.write((int) (i >> 8) & 0xFF);
        os.write((int) (i >> 16) & 0xFF);
        os.write((int) (i >> 24) & 0xFF);
        return i;
    }

    private int writeShort(OutputStream os, int i) throws IOException {
        os.write(i & 0xFF);
        os.write((i >> 8) & 0xFF);
        return i;
    }

    /**
     * Writes data for the current entry to the underlying stream.
     *
     * @exception IOException
     *                If an error occurs writing to the stream
     */
    @Override
    public void write(byte[] buffer, int offset, int byteCount) throws IOException {
        Arrays.checkOffsetAndCount(buffer.length, offset, byteCount);
        if (currentEntry == null) {
            throw new ZipException("No active entry");
        }

        if (currentEntry.getMethod() == STORED) {
            out.write(buffer, offset, byteCount);
        } else {
            super.write(buffer, offset, byteCount);
        }
        crc.update(buffer, offset, byteCount);
    }

    private void checkOpen() throws IOException {
        if (cDir == null) {
            throw new IOException("Stream is closed");
        }
    }
}
