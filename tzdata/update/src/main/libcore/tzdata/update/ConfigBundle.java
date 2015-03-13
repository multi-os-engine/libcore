package libcore.tzdata.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A configuration bundle. This is a thin wrapper around some in-memory bytes representing a zip
 * archive and logic for its safe extraction.
 */
public final class ConfigBundle {

    /** The name of the file inside the bundle containing the TZ data version. */
    public static final String TZ_DATA_VERSION_FILE_NAME = "tzdata_version";

    /** The name of the file inside the bundle containing the expected device checksums. */
    public static final String CHECKSUMS_FILE_NAME = "checksums";

    /** The name of the file inside the bundle containing bionic/libcore TZ data. */
    public static final String ZONEINFO_FILE_NAME = "tzdata";

    /** The name of the file inside the bundle containing ICU TZ data. */
    public static final String ICU_DATA_FILE_NAME = "icu_tzdata.dat";

    private static final int BUFFER_SIZE = 8192;

    private final byte[] bytes;

    public ConfigBundle(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBundleBytes() {
        return bytes;
    }

    public void extractTo(File targetDir) throws IOException {
        extractZipSafely(new ByteArrayInputStream(bytes), targetDir, true /* makeWorldReadable */);
    }

    /** Visible for testing */
    static void extractZipSafely(InputStream is, File targetDir, boolean makeWorldReadable)
            throws IOException {

        try (ZipInputStream zipInputStream = new ZipInputStream(is)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                // Validate the entry name: make sure the unpacked file will exist beneath the
                // workingDir.
                String name = entry.getName();
                File entryFile = FileUtils.createSubFile(targetDir, name);

                if (entry.isDirectory()) {
                    FileUtils.ensureDirectoryExists(entryFile);
                    if (makeWorldReadable) {
                        FileUtils.makeDirectoryWorldAccessible(entryFile);
                    }
                } else {
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        int count;
                        while ((count = zipInputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, count);
                        }
                        // sync to disk
                        fos.getFD().sync();
                    }
                    // mark entryFile -rw-r--r--
                    if (makeWorldReadable) {
                        FileUtils.makeWorldReadable(entryFile);
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigBundle that = (ConfigBundle) o;

        if (!Arrays.equals(bytes, that.bytes)) {
            return false;
        }

        return true;
    }

}
