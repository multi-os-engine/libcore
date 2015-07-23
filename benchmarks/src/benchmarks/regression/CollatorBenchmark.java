package benchmarks.regression;

import com.google.caliper.SimpleBenchmark;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Locale;

public class CollatorBenchmark extends SimpleBenchmark {

    public void timeCollatorPrimary(int reps) {
        RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Locale.US);
        collator.setStrength(Collator.PRIMARY);
        for (int i = 0; i < reps; i++) {
            collator.compare("abcde", "abcdf");
            collator.compare("abcde", "abcde");
            collator.compare("abcdf", "abcde");
        }
    }

    public void timeCollatorSecondary(int reps) {
        RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Locale.US);
        collator.setStrength(Collator.SECONDARY);
        for (int i = 0; i < reps; i++) {
            collator.compare("abcdÂ", "abcdÄ");
            collator.compare("abcdÂ", "abcdÂ");
            collator.compare("abcdÄ", "abcdÂ");
        }
    }

    public void timeCollatorTertiary(int reps) {
        RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Locale.US);
        collator.setStrength(Collator.TERTIARY);
        for (int i = 0; i < reps; i++) {
            collator.compare("abcdE", "abcde");
            collator.compare("abcde", "abcde");
            collator.compare("abcde", "abcdE");
        }
    }

    public void timeCollatorIdentical(int reps) {
        RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Locale.US);
        collator.setStrength(Collator.IDENTICAL);
        for (int i = 0; i < reps; i++) {
            collator.compare("abcdȪ", "abcdȫ");
            collator.compare("abcdȪ", "abcdȪ");
            collator.compare("abcdȫ", "abcdȪ");
        }
    }
}
