package libcore.java.math;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunCSVTests extends junit.framework.TestCase {
	public static final String[] csvFileNames = { "/math_tests.csv",
			"/math_important_numbers.csv", "/math_java_only.csv" };

	/*
	 * csv file should have the following format:
	 * function,expected_output,input,extra(extreme, denorm, etc. : will be
	 * printed on test failure) looks for csv files in test/resources run with
	 * vogar --classpath out/target/common/obj/JAVA_LIBRARIES
	 * /core-tests-support_intermediates/javalib.jar
	 * libcore/luni/src/test/java/libcore/java/math/RunCSVTests.java
	 */

	public void test_CSV_inputs() {
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

	public void runTest(String[] testCase) {
		String function = testCase[0];
		double expectedOutput = Double.parseDouble(testCase[1]);
		double input = Double.parseDouble(testCase[2]);
		String extra = "";
		if (testCase.length > 3) {
			extra = testCase[3];
		}
		runTest(function, expectedOutput, input, extra);
	}

	public void runTest(String func, double expectedOutput, double input,
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
		}
	}

	private void forceFail() {
		// should fix, temporary hack to force a fail message when file not
		// found
		assertTrue("File not found", false);
	}

	public void runCosTest(double expectedOutput, double input, String extra) {
		double output = Math.cos(input);
		double one_ulp = Math.ulp(expectedOutput);
		assertEquals(extra + ": cos:" + input + " : ", expectedOutput, output,
				one_ulp);
	}

	public void runSinTest(double expectedOutput, double input, String extra) {
		double output = Math.sin(input);
		double one_ulp = Math.ulp(expectedOutput);
		assertEquals(extra + ": sin:" + input + " : ", expectedOutput, output,
				one_ulp);
	}

	public void runLogTest(double expectedOutput, double input, String extra) {
		double output = Math.log(input);
		double one_ulp = Math.ulp(expectedOutput);
		assertEquals(extra + ": log:" + input + " : ", expectedOutput, output,
				one_ulp);
	}

	public void runTanTest(double expectedOutput, double input, String extra) {
		double output = Math.tan(input);
		double one_ulp = Math.ulp(expectedOutput);
		assertEquals(extra + ": tan:" + input + " : ", expectedOutput, output,
				one_ulp);
	}

	public void runExpTest(double expectedOutput, double input, String extra) {
		double output = Math.exp(input);
		double one_ulp = Math.ulp(expectedOutput);
		assertEquals(extra + ": exp:" + input + " : ", expectedOutput, output,
				one_ulp);
	}

	public void runCoshTest(double expectedOutput, double input, String extra) {
		double output = Math.cosh(input);
		assertEquals(extra + ": cosh:" + input + " : ", expectedOutput, output,
				2.5 * Math.ulp(expectedOutput));
	}

	public void runSinhTest(double expectedOutput, double input, String extra) {
		double output = Math.sinh(input);
		assertEquals(extra + ": sinh:" + input + " : ", expectedOutput, output,
				2.5 * Math.ulp(expectedOutput));
	}

	public void runLog10Test(double expectedOutput, double input,
			String extra) {
		double output = Math.log10(input);
		assertEquals(extra + ": log10:" + input + " : ", expectedOutput, output,
				Math.ulp(expectedOutput));
	}

	public void runTanhTest(double expectedOutput, double input, String extra) {
		double output = Math.tanh(input);
		assertEquals(extra + ": tanh:" + input + " : ", expectedOutput, output,
				2.5 * Math.ulp(expectedOutput));
	}

	public void runAcosTest(double expectedOutput, double input, String extra) {
		double output = Math.acos(input);
		assertEquals(extra + ": acos:" + input + " : ", expectedOutput, output,
				Math.ulp(expectedOutput));
	}

	public void runAsinTest(double expectedOutput, double input, String extra) {
		double output = Math.asin(input);
		assertEquals(extra + ": asin:" + input + " : ", expectedOutput, output,
				Math.ulp(expectedOutput));
	}

	public void runAtanTest(double expectedOutput, double input, String extra) {
		double output = Math.atan(input);
		assertEquals(extra + ": atan:" + input + " : ", expectedOutput, output,
				Math.ulp(expectedOutput));
	}

	public void runSqrtTest(double expectedOutput, double input, String extra) {
		double output = Math.sqrt(input);
		assertEquals(extra + ": sqrt:" + input + " : ", expectedOutput, output,
				0D);
	}

	public void runAbsTest(double expectedOutput, double input, String extra) {
		double output = Math.abs(input);
		assertEquals(extra + ": abs:" + input + " : ", expectedOutput, output,
				0D);
	}

	public void runExpm1Test(double expectedOutput, double input,
			String extra) {
		double output = StrictMath.expm1(input);
		assertEquals(extra + ": expm1:" + input + " : ", expectedOutput, output,
				Math.ulp(expectedOutput));
	}

	public void runCeilTest(double expectedOutput, double input, String extra) {
		double output = StrictMath.ceil(input);
		assertEquals(extra + ": ceil:" + input + " : ", expectedOutput, output,
				0D);
	}

	public void runFloorTest(double expectedOutput, double input,
			String extra) {
		double output = StrictMath.floor(input);
		assertEquals(extra + ": floor:" + input + " : ", expectedOutput, output,
				0D);
	}

	public void runLog1pTest(double expectedOutput, double input,
			String extra) {
		double output = StrictMath.log1p(input);
		assertEquals(extra + ": log1p:" + input + " : ", expectedOutput, output,
				Math.ulp(expectedOutput));
	}

	public void runNextAfterTest(double expectedOutput, double input,
			String extra) {
		double output = StrictMath.nextAfter(input, 2.3);
		assertEquals(extra + ": nextAfter:" + input + " : ", expectedOutput,
				output, 0D);
	}

	public void runCbrtTest(double expectedOutput, double input, String extra) {
		double output = StrictMath.cbrt(input);
		assertEquals(extra + ": cbrt:" + input + " : ", expectedOutput, output,
				Math.ulp(expectedOutput));
	}

	public void runGetExponentTest(double expectedOutput, double input,
			String extra) {
		double output = StrictMath.getExponent(input);
		assertEquals(extra + ": getExponent:" + input + " : ", expectedOutput,
				output, 0D);
	}

	public void runNextUpTest(double expectedOutput, double input,
			String extra) {
		double output = StrictMath.nextUp(input);
		assertEquals(extra + ": nextUp:" + input + " : ", expectedOutput,
				output, 0D);
	}

	public void runSignumTest(double expectedOutput, double input,
			String extra) {
		double output = StrictMath.signum(input);
		assertEquals(extra + ": signum:" + input + " : ", expectedOutput,
				output, 0D);
	}

	public void runToDegreesTest(double expectedOutput, double input,
			String extra) {
		double output = StrictMath.toDegrees(input);
		assertEquals(extra + ": toDegrees:" + input + " : ", expectedOutput,
				output, 0D);
	}

	public void runToRadiansTest(double expectedOutput, double input,
			String extra) {
		double output = StrictMath.toRadians(input);
		assertEquals(extra + ": toRadians:" + input + " : ", expectedOutput,
				output, 0D);
	}

	public void runUlpTest(double expectedOutput, double input, String extra) {
		double output = StrictMath.ulp(input);
		assertEquals(extra + ": ulp:" + input + " : ", expectedOutput, output,
				0D);
	}

	public void runRintTest(double expectedOutput, double input, String extra) {
		double output = StrictMath.rint(input);
		assertEquals(extra + ": rint:" + input + " : ", expectedOutput, output,
				0D);
	}

}
