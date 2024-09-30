package java.lang;

import libcore.io.Libcore;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class ProcessHandleImpl implements ProcessHandle {

    private static final ProcessHandleImpl current = new ProcessHandleImpl(Libcore.os.getpid());

    private final int pid;

    private ProcessHandleImpl(int pid) {
        this.pid = pid;
    }

    public static ProcessHandle current() {
        return current;
    }

    @Override
    public long pid() {
        return pid;
    }

    @Override
    public Optional<ProcessHandle> parent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<ProcessHandle> children() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<ProcessHandle> descendants() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Info info() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<ProcessHandle> onExit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsNormalTermination() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean destroy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean destroyForcibly() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAlive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(ProcessHandle other) {
        return Long.compare(pid, ((ProcessHandleImpl) other).pid);
    }

    @Override
    public String toString() {
        return Long.toString(pid);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(pid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcessHandleImpl)) return false;

        ProcessHandleImpl that = (ProcessHandleImpl)o;
        return pid == that.pid;
    }
}
