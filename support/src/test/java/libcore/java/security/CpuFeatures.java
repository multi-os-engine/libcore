package libcore.java.security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CpuFeatures {
    private CpuFeatures() {
    }

    public static boolean isAESHardwareAccelerated() {
        List<String> features = getListFromCpuinfo("Features");
        if (features != null && features.contains("aes")) {
            return true;
        }

        List<String> flags = getListFromCpuinfo("flags");
        if (flags != null && flags.contains("aes")) {
            return true;
        }

        return false;
    }

    private static String getFieldFromCpuinfo(String field) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
            Pattern p = Pattern.compile(field + "\\s*:\\s*(.*)");

            try {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        return m.group(1);
                    }
                }
            } finally {
                br.close();
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    private static List<String> getListFromCpuinfo(String fieldName) {
        String features = getFieldFromCpuinfo(fieldName);
        if (features == null)
            return null;

        return Arrays.asList(features.split("\\s"));
    }
}
