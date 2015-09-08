package benchmarks.regression;

import java.math.BigDecimal;

public class BigDecimalBenchmark {

    public void time_newBigDecimal(int reps) {
        for (int i = 0; i < reps; i++) {
            new BigDecimal("1E1234");
        }
    }




}
