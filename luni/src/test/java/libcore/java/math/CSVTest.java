package libcore.java.math;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * TODO: Insert description here. (generated by themichaelchen)
 */
public abstract class CSVTest extends junit.framework.TestCase {
    
    /*
     * csv file should have the following format:
     * function,expected_output,input,extra_info
     * vogar classpath: obj/JAVA_LIBRARIES/core-tests-support_intermediates
     */
    
    
    void TestCSVInputs(String[] csvFileNames) {
        int totalTests = 0;
        for (String csvFileName : csvFileNames) {
            String line = "";
            BufferedReader br = null;

            try {
                br = new BufferedReader(new InputStreamReader(
                        getClass().getResourceAsStream(csvFileName)));
                while ((line = br.readLine()) != null) {
                    String[] testCase = line.split(",");
                    runTest(testCase);
                    totalTests++;
                }
            } catch (FileNotFoundException e) {
                System.err.println("FileNotFound: " + csvFileName);
                forceFail();
            } catch (IOException e) {
                e.printStackTrace();
                forceFail();
            } catch (NullPointerException e) {
                System.err.println("NullPointer: FileNotFound: " + csvFileName);
                forceFail();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Completed running " + totalTests + " tests");
    }
    
    
    protected void runTest(String[] testCase) {
        String function = testCase[0];
        double expectedOutput = Double.parseDouble(testCase[1]);
        double input = Double.parseDouble(testCase[2]);
        String extra = "";
        if (testCase.length > 3) {
            extra = testCase[3];
        }
        runTest(function, expectedOutput, input, extra);
    }
    
    protected void runTest(String func, double expectedOutput, double input,
            String extra) {
        switch (func) {
            case "cos":
                runCosTest(expectedOutput, input, extra);
                break;
            case "sin":
                runSinTest(expectedOutput, input, extra);
                break;
            case "tan":
                runTanTest(expectedOutput, input, extra);
                break;
            case "log":
                runLogTest(expectedOutput, input, extra);
                break;
            case "exp":
                runExpTest(expectedOutput, input, extra);
                break;
            case "log10":
                runLog10Test(expectedOutput, input, extra);
                break;
            case "abs":
                runAbsTest(expectedOutput, input, extra);
                break;
            case "acos":
                runAcosTest(expectedOutput, input, extra);
                break;
            case "asin":
                runAsinTest(expectedOutput, input, extra);
                break;
            case "atan":
                runAtanTest(expectedOutput, input, extra);
                break;
            case "cosh":
                runCoshTest(expectedOutput, input, extra);
                break;
            case "sinh":
                runSinhTest(expectedOutput, input, extra);
                break;
            case "tanh":
                runTanhTest(expectedOutput, input, extra);
                break;
            case "sqrt":
                runSqrtTest(expectedOutput, input, extra);
                break;
            case "ceil":
                runCeilTest(expectedOutput, input, extra);
                break;
            case "floor":
                runFloorTest(expectedOutput, input, extra);
                break;
            case "log1p":
                runLog1pTest(expectedOutput, input, extra);
                break;
            case "nextAfter":
                runNextAfterTest(expectedOutput, input, extra);
                break;
            case "expm1":
                runExpm1Test(expectedOutput, input, extra);
                break;
            case "cbrt":
                runCbrtTest(expectedOutput, input, extra);
                break;
            case "getExponent":
                runGetExponentTest(expectedOutput, input, extra);
                break;
            case "nextUp":
                runNextUpTest(expectedOutput, input, extra);
                break;
            case "toDegrees":
                runToDegreesTest(expectedOutput, input, extra);
                break;
            case "toRadians":
                runToRadiansTest(expectedOutput, input, extra);
                break;
            case "rint":
                runRintTest(expectedOutput, input, extra);
                break;
            case "ulp":
                runUlpTest(expectedOutput, input, extra);
                break;
            case "signum":
                runSignumTest(expectedOutput, input, extra);
                break;
            default:
                System.err.println("Unknown function: " + func);
                forceFail();
        }
    }

    private void forceFail() {
        assertTrue(false);
    }
    
    
    
    public abstract void runCosTest(double expectedOutput, double input,
            String extra);

    public abstract void runSinTest(double expectedOutput, double input,
            String extra);

    public abstract void runTanTest(double expectedOutput, double input,
            String extra);

    public abstract void runLogTest(double expectedOutput, double input,
            String extra);

    public abstract void runExpTest(double expectedOutput, double input,
            String extra);

    public abstract void runLog10Test(double expectedOutput, double input,
            String extra);

    public abstract void runAbsTest(double expectedOutput, double input,
            String extra);

    public abstract void runAsinTest(double expectedOutput, double input,
            String extra);
    
    public abstract void runAcosTest(double expectedOutput, double input,
            String extra);

    public abstract void runAtanTest(double expectedOutput, double input,
            String extra);

    public abstract void runCoshTest(double expectedOutput, double input,
            String extra);

    public abstract void runSinhTest(double expectedOutput, double input,
            String extra);

    public abstract void runTanhTest(double expectedOutput, double input,
            String extra);

    public abstract void runSqrtTest(double expectedOutput, double input,
            String extra);

    public abstract void runCeilTest(double expectedOutput, double input,
            String extra);

    public abstract void runFloorTest(double expectedOutput, double input,
            String extra);

    public abstract void runLog1pTest(double expectedOutput, double input,
            String extra);

    public abstract void runNextAfterTest(double expectedOutput, double input,
            String extra);
    
    public abstract void runNextUpTest(double expectedOutput, double input,
            String extra);

    public abstract void runExpm1Test(double expectedOutput, double input,
            String extra);

    public abstract void runCbrtTest(double expectedOutput, double input,
            String extra);

    public abstract void runGetExponentTest(double expectedOutput, double input,
            String extra);

    public abstract void runToDegreesTest(double expectedOutput, double input,
            String extra);

    public abstract void runToRadiansTest(double expectedOutput, double input,
            String extra);

    public abstract void runRintTest(double expectedOutput, double input,
            String extra);

    public abstract void runUlpTest(double expectedOutput, double input,
            String extra);

    public abstract void runSignumTest(double expectedOutput, double input,
            String extra);

}
