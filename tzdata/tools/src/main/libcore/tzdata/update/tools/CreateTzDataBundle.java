package libcore.tzdata.update.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Properties;
import libcore.tzdata.update.ConfigBundle;
import libcore.tzdata.update.FileUtils;

/**
 * A command-line tool for creating a TZ data update bundle.
 */
public class CreateTzDataBundle {

    private CreateTzDataBundle() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            printUsage();
            System.exit(1);
        }
        File f = new File(args[0]);
        if (!f.exists()) {
            printUsage();
            System.exit(2);
        }
        Properties p = loadProperties(f);
        TzDataBundleBuilder builder = new TzDataBundleBuilder()
                .setTzDataVersion(getMandatoryProperty(p, "tzdata.version"))
                .addBionicTzData(getMandatoryPropertyFile(p, "bionic.file"))
                .addIcuTzData(getMandatoryPropertyFile(p, "icu.file"));

        int i = 1;
        while (true) {
            String localFile = p.getProperty("checksum.file.local." + i);
            if (localFile == null) {
                break;
            }
            long checksum = FileUtils.calculateChecksum(new File(localFile));

            String deviceFileName = p.getProperty("checksum.file.ondevice." + i);
            if (deviceFileName == null) {
                break;
            }
            builder.addChecksum(deviceFileName, checksum);
            i++;
        }
        if (i == 1) {
            System.out.println("There must be at least one checksum file");
        }

        ConfigBundle bundle = builder.build();
        File outputFile = new File(args[1]);
        try (OutputStream os = new FileOutputStream(outputFile)) {
            os.write(bundle.getBundleBytes());
        }
        System.out.println("Wrote: " + outputFile);
    }

    private static File getMandatoryPropertyFile(Properties p, String propertyName) {
        String fileName = getMandatoryProperty(p, propertyName);
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println(
                    "Missing file: " + file + " for property " + propertyName + " does not exist.");
            printUsage();
            System.exit(4);
        }
        return file;
    }

    private static String getMandatoryProperty(Properties p, String propertyName) {
        String value = p.getProperty(propertyName);
        if (value == null) {
            System.out.println("Missing property: " + propertyName);
            printUsage();
            System.exit(3);
        }
        return value;
    }

    private static Properties loadProperties(File f) throws IOException {
        Properties p = new Properties();
        try (Reader reader = new InputStreamReader(new FileInputStream(f))) {
            p.load(reader);
        }
        return p;
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("\t" + CreateTzDataBundle.class.getName() +
                " <tzupdate.properties file> <output file>");
    }
}
