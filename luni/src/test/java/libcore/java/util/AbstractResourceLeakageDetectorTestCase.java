package libcore.java.util;

import junit.framework.TestCase;

/**
 * Ensures that resources used within a test are cleaned up; will detect problems with tests and
 * also with runtime.
 *
 * <p>The underlying CloseGuardMonitor is loaded using reflection to ensure that this will run,
 * albeit doing nothing, on the reference implementation.
 */
public abstract class AbstractResourceLeakageDetectorTestCase extends TestCase {

    private static final Class<?> CLOSE_GUARD_MONITOR_CLASS;

    static {
        ClassLoader classLoader = AbstractResourceLeakageDetectorTestCase.class.getClassLoader();
        Class<?> clazz;
        try {
            clazz = classLoader.loadClass("dalvik.system.CloseGuardMonitor");
        } catch (ClassNotFoundException e) {
            System.logW("Resource leakage will not be detected; "
                    + "this is expected in the reference implementation", e);

            // Ignore, probably running in reference implementation.
            clazz = null;
        }

        CLOSE_GUARD_MONITOR_CLASS = clazz;
    }

    /**
     * The underlying CloseGuardMonitor that will perform the post test checks for resource
     * leakage.
     */
    private Runnable postTestChecker;

    @Override
    protected void setUp() throws Exception {

        if (CLOSE_GUARD_MONITOR_CLASS != null) {
            System.logI("Creating CloseGuard monitor");
            postTestChecker = (Runnable) CLOSE_GUARD_MONITOR_CLASS.newInstance();
        }
    }

    @Override
    protected void tearDown() throws Exception {

        // If available check for resource leakage.
        if (postTestChecker != null) {
            postTestChecker.run();
        }
    }
}
