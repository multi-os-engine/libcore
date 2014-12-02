package tests.util;

/**
 * Runner which executes the provided code under test (via a callback) for each provided input
 * value.
 */
public final class ForEachRunner {

    /**
     * Callback parameterized with a value.
     */
    public interface Callback<T> {
        /**
         * Invokes the callback for the provided value.
         */
        void run(T value) throws Exception;
    }

    private ForEachRunner() {}

    /**
     * Invokes the provided callback for each of the provided values.
     */
    public static <T> void run(Callback<T> callback, Iterable<T> values) throws Exception {
        for (T value : values) {
            try {
                callback.run(value);
            } catch (Throwable e) {
                throw new Exception("Failed for " + value + ": " + e.getMessage(), e);
            }
        }
    }
}
