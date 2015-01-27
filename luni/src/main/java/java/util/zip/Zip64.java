/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package java.util.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.util.zip.ZipOutputStream.writeIntAsUint16;
import static java.util.zip.ZipOutputStream.writeLongAsUint32;
import static java.util.zip.ZipOutputStream.writeLongAsUint64;

public class Zip64 {

    /* Non instantiable */
    private Zip64() {}

    /**
     * The maximum supported entry / archive size for standard (non zip64) entries and archives.
     */
    static final long MAX_ZIP_ENTRY_AND_ARCHIVE_SIZE = 0x00000000ffffffffL;

    /**
     * The header ID of the zip64 extended info header. This value is used to identify
     * zip64 data in the "extra" field in the file headers.
     */
    private static final short ZIP64_EXTENDED_INFO_HEADER_ID = 0x0001;

    /**
     * The minimum size of the zip64 extended info header. This excludes the 2 byte header ID
     * and the 2 byte size.
     */
    private static final int ZIP64_EXTENDED_INFO_MIN_SIZE = 28;

    /*
     * Size (in bytes) of the zip64 end of central directory locator. This will be located
     * immediately before the end of central directory record if a given zipfile is in the
     * zip64 format.
     */
    private static final int ZIP64_LOCATOR_SIZE = 20;

    /**
     * The zip64 end of central directory locator signature (4 bytes wide).
     */
    private static final int ZIP64_LOCATOR_SIGNATURE = 0x07064b50;

    /**
     * The zip64 end of central directory record singature (4 bytes wide).
     */
    private static final int ZIP64_EOCD_RECORD_SIGNATURE = 0x06064b50;

    /**
     * The "effective" size of the zip64 eocd record. This excludes the fields that
     * are proprietary, signature, or fields we aren't interested in. We include the
     * following (contiguous) fields in this calculation :
     * - disk number (4 bytes)
     * - disk with start of central directory (4 bytes)
     * - number of central directory entries on this disk (8 bytes)
     * - total number of central directory entries (8 bytes)
     * - size of the central directory (8 bytes)
     * - offset of the start of the central directory (8 bytes)
     */
    private static final int ZIP64_EOCD_RECORD_EFFECTIVE_SIZE = 40;

    /**
     * Parses the zip64 end of central directory record locator. This
     */
    public static long parseZip64EocdRecordLocator(RandomAccessFile raf, long eocdOffset) throws IOException {
        // The spec stays curiously silent about whether a zip file with an EOCD record, a zip64 locator
        // and a zip64 eocd record is considered "empty". For the purposes of this implementation,
        if (eocdOffset > ZIP64_LOCATOR_SIZE) {
            raf.seek(eocdOffset - ZIP64_LOCATOR_SIZE);
            if (Integer.reverseBytes(raf.readInt()) == ZIP64_LOCATOR_SIGNATURE) {
                byte[] zip64EocdLocator = new byte[ZIP64_LOCATOR_SIZE  - 4];
                raf.readFully(zip64EocdLocator);
                ByteBuffer buf = ByteBuffer.wrap(zip64EocdLocator).order(ByteOrder.LITTLE_ENDIAN);

                final int diskWithCentralDir = buf.getInt();
                final long zip64EocdRecordOffset = buf.getLong();
                final int numDisks = buf.getInt();

                if (numDisks != 1 || diskWithCentralDir != 0) {
                    throw new ZipException("Spanned archives not supported");
                }

                return zip64EocdRecordOffset;
            }
        }

        return -1;
    }

    public static ZipFile.EocdRecord parseZip64EocdRecord(RandomAccessFile raf,
            long eocdRecordOffset, int commentLength) throws IOException {
        raf.seek(eocdRecordOffset);
        final int signature = Integer.reverseBytes(raf.readInt());
        if (signature != ZIP64_EOCD_RECORD_SIGNATURE) {
            throw new ZipException("Invalid zip64 eocd record offset, sig=" + Integer.toHexString(signature)
                    + " offset=" + eocdRecordOffset);
        }

        // The zip64 eocd record specifies its own size as an 8 byte integral type. It is variable length
        // because of the "zip64 extensible data sector" but that field is reserved for pkware's
        // proprietary use. We therefore disregard it altogether and treat the end of central directory
        // structure as fixed length.
        //
        // We also skip "version made by" (2 bytes) and "version needed to extract" (2 bytes) fields. We perform
        // additional validation at the ZipEntry level, where applicable.
        //
        // That's a total of 12 bytes to skip
        raf.skipBytes(12);

        byte[] zip64Eocd = new byte[ZIP64_EOCD_RECORD_EFFECTIVE_SIZE];
        raf.readFully(zip64Eocd);

        ByteBuffer buf = ByteBuffer.wrap(zip64Eocd).order(ByteOrder.LITTLE_ENDIAN);
        int diskNumber = buf.getInt();
        int diskWithCentralDirStart = buf.getInt();
        long numEntries = buf.getLong();
        long totalNumEntries = buf.getLong();
        buf.getLong(); // Ignore the size of the central directory
        long centralDirOffset = buf.getLong();

        if (numEntries != totalNumEntries || diskNumber != 0 || diskWithCentralDirStart != 0) {
            throw new ZipException("Spanned archives not supported");
        }

        return new ZipFile.EocdRecord(numEntries, centralDirOffset, commentLength);
    }

    public static boolean parseZip64ExtendedInfo(ZipEntry ze, boolean centralDirectory) throws ZipException {
        boolean hasZip64ExtendedInfo = false;

        // If this file contains a zip64 central directory locator, entries might
        // optional contain a zip64 extended information extra entry.
        if (ze.extra != null && ze.extra.length > 0) {
            // Extensible data fields are of the form header1+data1 + header2+data2 and so
            // on, where each header consists of a 2 byte header ID followed by a 2 byte size.
            // We need to iterate through the entire list of headers to find the header ID
            // for the zip64 extended information extra field (0x0001).
            final ByteBuffer buf = ByteBuffer.wrap(ze.extra).order(ByteOrder.LITTLE_ENDIAN);
            while (buf.hasRemaining()) {
                if (buf.getShort() == ZIP64_EXTENDED_INFO_HEADER_ID) {
                    hasZip64ExtendedInfo = true;
                    break;
                } else {
                    buf.position(buf.position() + buf.getShort());
                }
            }

            // We should bother looking at the size & compressed size
            if (hasZip64ExtendedInfo) {
                final int extendedInfoSize = buf.getShort() & 0xFFFF;
                if (extendedInfoSize < ZIP64_EXTENDED_INFO_MIN_SIZE) {
                    throw new ZipException("Invalid zip64 extended info size: " + extendedInfoSize);
                }

                // The size & compressed size only make sense in the central directory *or* if
                // we know them beforehand. If we don't know them beforehand, they're stored in
                // the data descriptor and should be read from there.
                if (centralDirectory || (ze.getMethod() == ZipEntry.STORED)) {
                    final long zip64Size = buf.getLong();
                    if (ze.size == 0x00000000FFFFFFFFL) {
                        ze.size = zip64Size;
                    }

                    final long zip64CompressedSize = buf.getLong();
                    if (ze.compressedSize == 0x00000000FFFFFFFFL) {
                        ze.compressedSize = zip64CompressedSize;
                    }
                }

                // The local header offset is significant only in the central directory. It (obviously)
                // makes no sense within the local header itself.
                if (centralDirectory) {
                    final long zip64LocalHeaderRelOffset = buf.getLong();
                    if (ze.localHeaderRelOffset == 0x00000000FFFFFFFFL) {
                        ze.localHeaderRelOffset = zip64LocalHeaderRelOffset;
                    }
                }
            }
        }

        // This entry doesn't contain a zip64 extended information data entry header.
        // We have to check that the compressedSize / size / localHEaderRelOffset values
        // are valid and don't require the presence of the extended header.
        if (!hasZip64ExtendedInfo) {
            if (ze.compressedSize == 0x00000000FFFFFFFFL || ze.size == 0x00000000FFFFFFFFL ||
                    ze.localHeaderRelOffset == 0x00000000FFFFFFFFL) {
                throw new ZipException("File contains no zip64 extended information for central directory values: "
                        + "compressedSize=" + ze.compressedSize + ", size=" + ze.size
                        + ", localHeader=" + ze.localHeaderRelOffset);
            }
        }

        return hasZip64ExtendedInfo;
    }

    public static byte[] appendZip64ExtendedInfoToExtras(ZipEntry ze) {
        final byte[] output;
        if (ze.extra == null) {
            output = new byte[ZIP64_EXTENDED_INFO_MIN_SIZE + 4];
        } else {
            output = new byte[ze.extra.length + ZIP64_EXTENDED_INFO_MIN_SIZE + 4];
            System.arraycopy(ze.extra, 0, output, 0, ze.extra.length);
        }

        ByteBuffer bb = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN);
        if (ze.extra != null) {
            bb.position(ze.extra.length);
        }

        bb.putShort(ZIP64_EXTENDED_INFO_HEADER_ID);
        bb.putShort((short) ZIP64_EXTENDED_INFO_MIN_SIZE);

        if (ze.getMethod() == ZipEntry.STORED) {
            bb.putLong(ze.size);
            bb.putLong(ze.compressedSize);
        } else {
            // Store these fields in the data descriptor instead.
            bb.putLong(0); // size.
            bb.putLong(0); // compressed size.
        }

        // Note that the offset is only relevant in the central directory entry, but
        // we write it out here anyway, since we know what it is.
        bb.putLong(ze.localHeaderRelOffset);
        bb.putInt(0);  //  disk number

        return output;
    }

    private static boolean hasZip64ExtendedInfo(ByteBuffer buf) {
        boolean hasZip64ExtendedInfo = false;
        while (buf.hasRemaining()) {
            if (buf.getShort() == ZIP64_EXTENDED_INFO_HEADER_ID) {
                hasZip64ExtendedInfo = true;
                break;
            } else {
                buf.position(buf.position() + buf.getShort());
            }
        }

        return hasZip64ExtendedInfo;
    }

    public static void refreshZip64ExtendedInfo(ZipEntry ze) {
        if (ze.extra == null || ze.extra.length < ZIP64_EXTENDED_INFO_MIN_SIZE) {
            throw new IllegalStateException("Zip64 entry has no available extras");
        }


        ByteBuffer buf = ByteBuffer.wrap(ze.extra).order(ByteOrder.LITTLE_ENDIAN);
        if (!hasZip64ExtendedInfo(buf)) {
            throw new IllegalStateException("Zip64 entry extras has no zip64 extended info record");
        }

        // Discard the entry size.
        buf.position(buf.position() + 2);

        buf.putLong(ze.size);
        buf.putLong(ze.compressedSize);
        buf.putLong(ze.localHeaderRelOffset);
        buf.putInt(0); // disk number.
    }

    public static void writeZip64EocdRecordAndLocator(ByteArrayOutputStream baos,
            long numEntries, long offset, long cDirSize) throws IOException {
        // Step 1: Write out the zip64 EOCD record.
        writeLongAsUint32(baos, ZIP64_EOCD_RECORD_SIGNATURE);
        // The size of the zip64 eocd record. This is the effective size + the
        // size of the "version made by" and the "version needed to extract" fields.
        writeLongAsUint64(baos, ZIP64_EOCD_RECORD_EFFECTIVE_SIZE + 4);
        // TODO: What values should we put here ? The pre-zip64 values we've chosen don't
        // seem to make much sense either.
        writeIntAsUint16(baos, 20);
        writeIntAsUint16(baos, 20);
        writeLongAsUint32(baos, 0L); // number of disk
        writeLongAsUint32(baos, 0L); // number of disk with start of central dir.
        writeLongAsUint64(baos, numEntries); // number of entries in this disk.
        writeLongAsUint64(baos, numEntries); // number of entries in total.
        writeLongAsUint64(baos, cDirSize); // size of the central directory.
        writeLongAsUint64(baos, offset); // offset of the central directory wrt. this file.

        // Step 2: Write out the zip64 EOCD record locator.
        writeLongAsUint32(baos, ZIP64_LOCATOR_SIGNATURE);
        writeLongAsUint32(baos, 0); // number of disk with start of central dir.
        writeLongAsUint64(baos, offset + cDirSize); // offset of the eocd record wrt. this file.
        writeLongAsUint32(baos, 1); // total number of disks.
    }
}
