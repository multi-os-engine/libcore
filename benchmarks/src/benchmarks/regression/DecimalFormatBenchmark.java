package benchmarks.regression;

import com.google.caliper.SimpleBenchmark;

import java.math.BigInteger;
import java.text.DecimalFormat;

public class DecimalFormatBenchmark extends SimpleBenchmark {


    private static final String PATTERN = "##.##-'E'";
    private static final DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();

    private static final BigInteger bi = new BigInteger("1000000");

    public void time_setPattern(int reps) {
        for (int i = 0; i < reps; i++) {
            df.applyPattern(PATTERN);
        }
    }

    public void time_formatInt(int reps) {
        df.applyPattern(PATTERN);
        for (int i = 0; i < reps; i++) {
            df.format(1000000);
        }
    }

    public void time_formatDouble(int reps) {
        df.applyPattern(PATTERN);
        for (int i = 0; i < reps; i++) {
            df.format(1000000.0);
        }
    }

    public void time_formatBigInteger(int reps) {
        df.applyPattern(PATTERN);
        for (int i = 0; i < reps; i++) {
            df.format(1000000);
        }
    }

    public void time_format_groupingSize(int reps) {
        df.applyPattern(PATTERN);
        df.setGroupingSize(3);
        for (int i = 0; i < reps; i++) {
            df.format(1000000);
        }
    }

}
