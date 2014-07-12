package libcore.java.math;

public class RunCSVTestsStrict extends CSVTest {
    public static final String[] csvFileNames = { "/math_tests.csv",
            "/math_important_numbers.csv", "/math_java_only.csv" };

    public void test_csv() {
        this.TestCSVInputs(csvFileNames);
    }

    @Override
    public void runCosTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.cos(input);
        assertEquals(extra + ": cos:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runSinTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.sin(input);
        assertEquals(extra + ": sin:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runLogTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.log(input);
        assertEquals(extra + ": log:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runTanTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.tan(input);
        assertEquals(extra + ": tan:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runCoshTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.cosh(input);
        assertEquals(extra + ": cosh:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runSinhTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.sinh(input);
        assertEquals(extra + ": sinh:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runLog10Test(double expectedOutput, double input,
            String extra) {
        double output = StrictMath.log10(input);
        assertEquals(extra + ": log10:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runTanhTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.tanh(input);
        assertEquals(extra + ": tanh:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runExpTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.exp(input);
        assertEquals(extra + ": exp:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runExpm1Test(double expectedOutput, double input,
            String extra) {
        double output = StrictMath.expm1(input);
        assertEquals(extra + ": expm1:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runAcosTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.acos(input);
        assertEquals(extra + ": acos:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runAsinTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.asin(input);
        assertEquals(extra + ": asin:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runAtanTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.atan(input);
        assertEquals(extra + ": atan:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runSqrtTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.sqrt(input);
        assertEquals(extra + ": sqrt:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runAbsTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.abs(input);
        assertEquals(extra + ": abs:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runCeilTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.ceil(input);
        assertEquals(extra + ": ceil:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runFloorTest(double expectedOutput, double input,
            String extra) {
        double output = StrictMath.floor(input);
        assertEquals(extra + ": floor:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runLog1pTest(double expectedOutput, double input,
            String extra) {
        double output = StrictMath.log1p(input);
        assertEquals(extra + ": log1p:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runNextAfterTest(double expectedOutput, double input,
            String extra) {
        double output = StrictMath.nextAfter(input, 2.3);
        assertEquals(extra + ": nextAfter:" + input + " : ", expectedOutput,
                output, 0D);
    }

    @Override
    public void runCbrtTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.cbrt(input);
        assertEquals(extra + ": cbrt:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runGetExponentTest(double expectedOutput, double input,
            String extra) {
        double output = StrictMath.getExponent(input);
        assertEquals(extra + ": getExponent:" + input + " : ", expectedOutput,
                output, 0D);
    }

    @Override
    public void runNextUpTest(double expectedOutput, double input,
            String extra) {
        double output = StrictMath.nextUp(input);
        assertEquals(extra + ": nextUp:" + input + " : ", expectedOutput,
                output, 0D);
    }

    @Override
    public void runSignumTest(double expectedOutput, double input,
            String extra) {
        double output = StrictMath.signum(input);
        assertEquals(extra + ": signum:" + input + " : ", expectedOutput,
                output, 0D);
    }

    @Override
    public void runToDegreesTest(double expectedOutput, double input,
            String extra) {
        double output = StrictMath.toDegrees(input);
        assertEquals(extra + ": toDegrees:" + input + " : ", expectedOutput,
                output, 0D);
    }

    @Override
    public void runToRadiansTest(double expectedOutput, double input,
            String extra) {
        double output = StrictMath.toRadians(input);
        assertEquals(extra + ": toRadians:" + input + " : ", expectedOutput,
                output, 0D);
    }

    @Override
    public void runUlpTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.ulp(input);
        assertEquals(extra + ": ulp:" + input + " : ", expectedOutput, output,
                0D);
    }

    @Override
    public void runRintTest(double expectedOutput, double input, String extra) {
        double output = StrictMath.rint(input);
        assertEquals(extra + ": rint:" + input + " : ", expectedOutput, output,
                0D);
    }

}
