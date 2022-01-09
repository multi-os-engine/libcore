/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

import java.nio.CharBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Objects;

/**
 * The base class for all readers. A reader is a means of reading data from a
 * source in a character-wise manner. Some readers also support marking a
 * position in the input and returning to this position later.
 * <p>
 * This abstract class does not provide a fully working implementation, so it
 * needs to be subclassed, and at least the {@link #read(char[], int, int)} and
 * {@link #close()} methods needs to be overridden. Overriding some of the
 * non-abstract methods is also often advised, since it might result in higher
 * efficiency.
 * <p>
 * Many specialized readers for purposes like reading from a file already exist
 * in this package.
 *
 * @see Writer
 */
public abstract class Reader implements Readable, Closeable {

    private static final int TRANSFER_BUFFER_SIZE = 8192;

    /**
     * Returns a new {@code Reader} that reads no characters. The returned
     * stream is initially open.  The stream is closed by calling the
     * {@code close()} method.  Subsequent calls to {@code close()} have no
     * effect.
     *
     * <p> While the stream is open, the {@code read()}, {@code read(char[])},
     * {@code read(char[], int, int)}, {@code read(Charbuffer)}, {@code
     * ready()}, {@code skip(long)}, and {@code transferTo()} methods all
     * behave as if end of stream has been reached. After the stream has been
     * closed, these methods all throw {@code IOException}.
     *
     * <p> The {@code markSupported()} method returns {@code false}.  The
     * {@code mark()} and {@code reset()} methods throw an {@code IOException}.
     *
     * <p> The {@link #lock object} used to synchronize operations on the
     * returned {@code Reader} is not specified.
     *
     * @return a {@code Reader} which reads no characters
     *
     * @since 11
     */
    public static Reader nullReader() {
        return new Reader() {
            private volatile boolean closed;

            private void ensureOpen() throws IOException {
                if (closed) {
                    throw new IOException("Stream closed");
                }
            }

            @Override
            public int read() throws IOException {
                ensureOpen();
                return -1;
            }

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                Objects.checkFromIndexSize(off, len, cbuf.length);
                ensureOpen();
                if (len == 0) {
                    return 0;
                }
                return -1;
            }

            @Override
            public int read(CharBuffer target) throws IOException {
                Objects.requireNonNull(target);
                ensureOpen();
                if (target.hasRemaining()) {
                    return -1;
                }
                return 0;
            }

            @Override
            public boolean ready() throws IOException {
                ensureOpen();
                return false;
            }

            @Override
            public long skip(long n) throws IOException {
                ensureOpen();
                return 0L;
            }

            @Override
            public long transferTo(Writer out) throws IOException {
                Objects.requireNonNull(out);
                ensureOpen();
                return 0L;
            }

            @Override
            public void close() {
                closed = true;
            }
        };
    }

    /**
     * The object used to synchronize access to the reader.
     */
    protected Object lock;

    /**
     * Constructs a new {@code Reader} with {@code this} as the object used to
     * synchronize critical sections.
     */
    protected Reader() {
        lock = this;
    }

    /**
     * Constructs a new {@code Reader} with {@code lock} used to synchronize
     * critical sections.
     *
     * @param lock
     *            the {@code Object} used to synchronize critical sections.
     * @throws NullPointerException
     *             if {@code lock} is {@code null}.
     */
    protected Reader(Object lock) {
        if (lock == null) {
            throw new NullPointerException("lock == null");
        }
        this.lock = lock;
    }

    /**
     * Closes this reader. Implementations of this method should free any
     * resources associated with the reader.
     *
     * @throws IOException
     *             if an error occurs while closing this reader.
     */
    public abstract void close() throws IOException;

    /**
     * Sets a mark position in this reader. The parameter {@code readLimit}
     * indicates how many characters can be read before the mark is invalidated.
     * Calling {@code reset()} will reposition the reader back to the marked
     * position if {@code readLimit} has not been surpassed.
     * <p>
     * This default implementation simply throws an {@code IOException};
     * subclasses must provide their own implementation.
     *
     * @param readLimit
     *            the number of characters that can be read before the mark is
     *            invalidated.
     * @throws IllegalArgumentException
     *             if {@code readLimit < 0}.
     * @throws IOException
     *             if an error occurs while setting a mark in this reader.
     * @see #markSupported()
     * @see #reset()
     */
    public void mark(int readLimit) throws IOException {
        throw new IOException();
    }

    /**
     * Indicates whether this reader supports the {@code mark()} and
     * {@code reset()} methods. This default implementation returns
     * {@code false}.
     *
     * @return always {@code false}.
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * Reads a single character from this reader and returns it as an integer
     * with the two higher-order bytes set to 0. Returns -1 if the end of the
     * reader has been reached.
     *
     * @return the character read or -1 if the end of the reader has been
     *         reached.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    public int read() throws IOException {
        synchronized (lock) {
            char[] charArray = new char[1];
            if (read(charArray, 0, 1) != -1) {
                return charArray[0];
            }
            return -1;
        }
    }

    /**
     * Reads characters from this reader and stores them in the character array
     * {@code buffer} starting at offset 0. Returns the number of characters
     * actually read or -1 if the end of the reader has been reached.
     *
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    public int read(char[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    /**
     * Reads up to {@code count} characters from this reader and stores them
     * at {@code offset} in the character array {@code buffer}. Returns the number
     * of characters actually read or -1 if the end of the reader has been
     * reached.
     *
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    public abstract int read(char[] buffer, int offset, int count) throws IOException;

    /**
     * Indicates whether this reader is ready to be read without blocking.
     * Returns {@code true} if this reader will not block when {@code read} is
     * called, {@code false} if unknown or blocking will occur. This default
     * implementation always returns {@code false}.
     *
     * @return always {@code false}.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     * @see #read()
     * @see #read(char[])
     * @see #read(char[], int, int)
     */
    public boolean ready() throws IOException {
        return false;
    }

    /**
     * Resets this reader's position to the last {@code mark()} location.
     * Invocations of {@code read()} and {@code skip()} will occur from this new
     * location. If this reader has not been marked, the behavior of
     * {@code reset()} is implementation specific. This default
     * implementation throws an {@code IOException}.
     *
     * @throws IOException
     *             always thrown in this default implementation.
     * @see #mark(int)
     * @see #markSupported()
     */
    public void reset() throws IOException {
        throw new IOException();
    }

    /**
     * Skips {@code charCount} characters in this reader. Subsequent calls of
     * {@code read} methods will not return these characters unless {@code
     * reset} is used. This method may perform multiple reads to read {@code
     * charCount} characters.
     *
     * @return the number of characters actually skipped.
     * @throws IllegalArgumentException
     *             if {@code charCount < 0}.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     * @see #mark(int)
     * @see #markSupported()
     * @see #reset()
     */
    public long skip(long charCount) throws IOException {
        if (charCount < 0) {
            throw new IllegalArgumentException("charCount < 0: " + charCount);
        }
        synchronized (lock) {
            long skipped = 0;
            int toRead = charCount < 512 ? (int) charCount : 512;
            char[] charsSkipped = new char[toRead];
            while (skipped < charCount) {
                int read = read(charsSkipped, 0, toRead);
                if (read == -1) {
                    return skipped;
                }
                skipped += read;
                if (read < toRead) {
                    return skipped;
                }
                if (charCount - skipped < toRead) {
                    toRead = (int) (charCount - skipped);
                }
            }
            return skipped;
        }
    }

    /**
     * Reads characters and puts them into the {@code target} character buffer.
     *
     * @param target
     *            the destination character buffer.
     * @return the number of characters put into {@code target} or -1 if the end
     *         of this reader has been reached before a character has been read.
     * @throws IOException
     *             if any I/O error occurs while reading from this reader.
     * @throws NullPointerException
     *             if {@code target} is {@code null}.
     * @throws ReadOnlyBufferException
     *             if {@code target} is read-only.
     */
    public int read(CharBuffer target) throws IOException {
        int length = target.length();
        char[] buf = new char[length];
        length = Math.min(length, read(buf));
        if (length > 0) {
            target.put(buf, 0, length);
        }
        return length;
    }

    /**
     * Reads all characters from this reader and writes the characters to the
     * given writer in the order that they are read. On return, this reader
     * will be at end of the stream. This method does not close either reader
     * or writer.
     * <p>
     * This method may block indefinitely reading from the reader, or
     * writing to the writer. The behavior for the case where the reader
     * and/or writer is <i>asynchronously closed</i>, or the thread
     * interrupted during the transfer, is highly reader and writer
     * specific, and therefore not specified.
     * <p>
     * If an I/O error occurs reading from the reader or writing to the
     * writer, then it may do so after some characters have been read or
     * written. Consequently the reader may not be at end of the stream and
     * one, or both, streams may be in an inconsistent state. It is strongly
     * recommended that both streams be promptly closed if an I/O error occurs.
     *
     * @param  out the writer, non-null
     * @return the number of characters transferred
     * @throws IOException if an I/O error occurs when reading or writing
     * @throws NullPointerException if {@code out} is {@code null}
     *
     * @since 10
     */
    public long transferTo(Writer out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        char[] buffer = new char[TRANSFER_BUFFER_SIZE];
        int nRead;
        while ((nRead = read(buffer, 0, TRANSFER_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, nRead);
            transferred += nRead;
        }
        return transferred;
    }

}
