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
     * Invokes the provided callback for each of the provided named values.
     *
     * @param namesAndValues named values represented as name-value pairs.
     *
     * @param <T> type of value.
     */
    public static <T> void runNamed(Callback<T> callback, Iterable<Pair<String, T>> namesAndValues)
            throws Exception {
        for (Pair<String, T> nameAndValue : namesAndValues) {
            try {
                callback.run(nameAndValue.getSecond());
            } catch (Throwable e) {
                throw new Exception("Failed for " + nameAndValue.getFirst() + ": " + e.getMessage(),
                        e);
            }
        }
    }
}
