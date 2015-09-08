package benchmarks.regression;

import com.google.caliper.SimpleBenchmark;

import java.util.Locale;

public class LocaleBenchmark extends SimpleBenchmark {

    private static final Locale withLanguageExtension = new Locale("de", "DE",
            "co-phonebk-kc-kv-space");

    private Locale[] set = new Locale[] {
            Locale.US,
            Locale.FRANCE,
            Locale.GERMANY,
            Locale.CHINA,
            Locale.FRANCE,
            Locale.KOREA,
            Locale.JAPAN,
            withLanguageExtension
    };

    public void time_toLanguageTag(int reps) {
        for (int i = 0; i < reps; i++) {
            for (Locale inSet : set) {
                inSet.toLanguageTag();
            }
        }
    }

    // Covers getDisplay{language, country, variant, script}.
    public void time_getDisplayName(int reps) {
        for (int i = 0; i < reps; i++) {
            for (Locale inSet : set) {
                inSet.getDisplayName();
            }
        }
    }

}
