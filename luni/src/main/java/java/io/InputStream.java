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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import libcore.io.Streams;

/**
 * A readable source of bytes.
 *
 * <p>Most clients will use input streams that read data from the file system
 * ({@link FileInputStream}), the network ({@link java.net.Socket#getInputStream()}/{@link
 * java.net.HttpURLConnection#getInputStream()}), or from an in-memory byte
 * array ({@link ByteArrayInputStream}).
 *
 * <p>Use {@link InputStreamReader} to adapt a byte stream like this one into a
 * character stream.
 *
 * <p>Most clients should wrap their input stream with {@link
 * BufferedInputStream}. Callers that do only bulk reads may omit buffering.
 *
 * <p>Some implementations support marking a position in the input stream and
 * resetting back to this position later. Implementations that don't return
 * false from {@link #markSupported()} and throw an {@link IOException} when
 * {@link #reset()} is called.
 *
 * <h3>Subclassing InputStream</h3>
 * Subclasses that decorate another input stream should consider subclassing
 * {@link FilterInputStream}, which delegates all calls to the source input
 * stream.
 *
 * <p>All input stream subclasses should override <strong>both</strong> {@link
 * #read() read()} and {@link #read(byte[],int,int) read(byte[],int,int)}. The
 * three argument overload is necessary for bulk access to the data. This is
 * much more efficient than byte-by-byte access.
 *
 * @see OutputStream
 */
public abstract class InputStream implements Closeable {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * This constructor does nothing. It is provided for signature
     * compatibility.
     */
    public InputStream() {
        /* empty */
    }

    /**
     * Returns a new {@code InputStream} that reads no bytes. The returned
     * stream is initially open.  The stream is closed by calling the
     * {@code close()} method.  Subsequent calls to {@code close()} have no
     * effect.
     *
     * <p> While the stream is open, the {@code available()}, {@code read()},
     * {@code read(byte[])}, {@code read(byte[], int, int)},
     * {@code readAllBytes()}, {@code readNBytes(byte[], int, int)},
     * {@code readNBytes(int)}, {@code skip(long)}, and
     * {@code transferTo()} methods all behave as if end of stream has been
     * reached.  After the stream has been closed, these methods all throw
     * {@code IOException}.
     *
     * <p> The {@code markSupported()} method returns {@code false}.  The
     * {@code mark()} method does nothing, and the {@code reset()} method
     * throws {@code IOException}.
     *
     * @return an {@code InputStream} which contains no bytes
     *
     * @since 11
     */
    public static InputStream nullInputStream() {
        return new InputStream() {
            private volatile boolean closed;

            private void ensureOpen() throws IOException {
                if (closed) {
                    throw new IOException("Stream closed");
                }
            }

            @Override
            public int available () throws IOException {
                ensureOpen();
                return 0;
            }

            @Override
            public int read() throws IOException {
                ensureOpen();
                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                Objects.checkFromIndexSize(off, len, b.length);
                if (len == 0) {
                    return 0;
                }
                ensureOpen();
                return -1;
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                ensureOpen();
                return new byte[0];
            }

            @Override
            public int readNBytes(byte[] b, int off, int len)
                throws IOException {
                Objects.checkFromIndexSize(off, len, b.length);
                ensureOpen();
                return 0;
            }

            @Override
            public byte[] readNBytes(int len) throws IOException {
                if (len < 0) {
                    throw new IllegalArgumentException("len < 0");
                }
                ensureOpen();
                return new byte[0];
            }

            @Override
            public long skip(long n) throws IOException {
                ensureOpen();
                return 0L;
            }

            @Override
            public long transferTo(OutputStream out) throws IOException {
                Objects.requireNonNull(out);
                ensureOpen();
                return 0L;
            }

            @Override
            public void close() throws IOException {
                closed = true;
            }
        };
    }

    /**
     * Returns an estimated number of bytes that can be read or skipped without blocking for more
     * input.
     *
     * <p>Note that this method provides such a weak guarantee that it is not very useful in
     * practice.
     *
     * <p>Firstly, the guarantee is "without blocking for more input" rather than "without
     * blocking": a read may still block waiting for I/O to complete&nbsp;&mdash; the guarantee is
     * merely that it won't have to wait indefinitely for data to be written. The result of this
     * method should not be used as a license to do I/O on a thread that shouldn't be blocked.
     *
     * <p>Secondly, the result is a
     * conservative estimate and may be significantly smaller than the actual number of bytes
     * available. In particular, an implementation that always returns 0 would be correct.
     * In general, callers should only use this method if they'd be satisfied with
     * treating the result as a boolean yes or no answer to the question "is there definitely
     * data ready?".
     *
     * <p>Thirdly, the fact that a given number of bytes is "available" does not guarantee that a
     * read or skip will actually read or skip that many bytes: they may read or skip fewer.
     *
     * <p>It is particularly important to realize that you <i>must not</i> use this method to
     * size a container and assume that you can read the entirety of the stream without needing
     * to resize the container. Such callers should probably write everything they read to a
     * {@link ByteArrayOutputStream} and convert that to a byte array. Alternatively, if you're
     * reading from a file, {@link File#length} returns the current length of the file (though
     * assuming the file's length can't change may be incorrect, reading a file is inherently
     * racy).
     *
     * <p>The default implementation of this method in {@code InputStream} always returns 0.
     * Subclasses should override this method if they are able to indicate the number of bytes
     * available.
     *
     * @return the estimated number of bytes available
     * @throws IOException if this stream is closed or an error occurs
     */
    public int available() throws IOException {
        return 0;
    }

    /**
     * Closes this stream. Concrete implementations of this class should free
     * any resources during close. This implementation does nothing.
     *
     * @throws IOException
     *             if an error occurs while closing this stream.
     */
    public void close() throws IOException {
        /* empty */
    }

    /**
     * Sets a mark position in this InputStream. The parameter {@code readlimit}
     * indicates how many bytes can be read before the mark is invalidated.
     * Sending {@code reset()} will reposition the stream back to the marked
     * position provided {@code readLimit} has not been surpassed.
     * <p>
     * This default implementation does nothing and concrete subclasses must
     * provide their own implementation.
     *
     * @param readlimit
     *            the number of bytes that can be read from this stream before
     *            the mark is invalidated.
     * @see #markSupported()
     * @see #reset()
     */
    public void mark(int readlimit) {
        /* empty */
    }

    /**
     * Indicates whether this stream supports the {@code mark()} and
     * {@code reset()} methods. The default implementation returns {@code false}.
     *
     * @return always {@code false}.
     * @see #mark(int)
     * @see #reset()
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * Reads a single byte from this stream and returns it as an integer in the
     * range from 0 to 255. Returns -1 if the end of the stream has been
     * reached. Blocks until one byte has been read, the end of the source
     * stream is detected or an exception is thrown.
     *
     * @throws IOException
     *             if the stream is closed or another IOException occurs.
     */
    public abstract int read() throws IOException;

    /**
     * Equivalent to {@code read(buffer, 0, buffer.length)}.
     */
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    /**
     * Reads up to {@code byteCount} bytes from this stream and stores them in
     * the byte array {@code buffer} starting at {@code byteOffset}.
     * Returns the number of bytes actually read or -1 if the end of the stream
     * has been reached.
     *
     * @throws IndexOutOfBoundsException
     *   if {@code byteOffset < 0 || byteCount < 0 || byteOffset + byteCount > buffer.length}.
     * @throws IOException
     *             if the stream is closed or another IOException occurs.
     */
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        Arrays.checkOffsetAndCount(buffer.length, byteOffset, byteCount);
        for (int i = 0; i < byteCount; ++i) {
            int c;
            try {
                if ((c = read()) == -1) {
                    return i == 0 ? -1 : i;
                }
            } catch (IOException e) {
                if (i != 0) {
                    return i;
                }
                throw e;
            }
            buffer[byteOffset + i] = (byte) c;
        }
        return byteCount;
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Reads all remaining bytes from the input stream. This method blocks until
     * all remaining bytes have been read and end of stream is detected, or an
     * exception is thrown. This method does not close the input stream.
     *
     * <p> When this stream reaches end of stream, further invocations of this
     * method will return an empty byte array.
     *
     * <p> Note that this method is intended for simple cases where it is
     * convenient to read all bytes into a byte array. It is not intended for
     * reading input streams with large amounts of data.
     *
     * <p> The behavior for the case where the input stream is <i>asynchronously
     * closed</i>, or the thread interrupted during the read, is highly input
     * stream specific, and therefore not specified.
     *
     * <p> If an I/O error occurs reading from the input stream, then it may do
     * so after some, but not all, bytes have been read. Consequently the input
     * stream may not be at end of stream and may be in an inconsistent state.
     * It is strongly recommended that the stream be promptly closed if an I/O
     * error occurs.
     *
     * @implSpec
     * This method invokes {@link #readNBytes(int)} with a length of
     * {@link Integer#MAX_VALUE}.
     *
     * @return a byte array containing the bytes read from this input stream
     * @throws IOException if an I/O error occurs
     * @throws OutOfMemoryError if an array of the required size cannot be
     *         allocated.
     *
     * @since 9
     */
    public byte[] readAllBytes() throws IOException {
        return readNBytes(Integer.MAX_VALUE);
    }

    /**
     * Reads up to a specified number of bytes from the input stream. This
     * method blocks until the requested number of bytes have been read, end
     * of stream is detected, or an exception is thrown. This method does not
     * close the input stream.
     *
     * <p> The length of the returned array equals the number of bytes read
     * from the stream. If {@code len} is zero, then no bytes are read and
     * an empty byte array is returned. Otherwise, up to {@code len} bytes
     * are read from the stream. Fewer than {@code len} bytes may be read if
     * end of stream is encountered.
     *
     * <p> When this stream reaches end of stream, further invocations of this
     * method will return an empty byte array.
     *
     * <p> Note that this method is intended for simple cases where it is
     * convenient to read the specified number of bytes into a byte array. The
     * total amount of memory allocated by this method is proportional to the
     * number of bytes read from the stream which is bounded by {@code len}.
     * Therefore, the method may be safely called with very large values of
     * {@code len} provided sufficient memory is available.
     *
     * <p> The behavior for the case where the input stream is <i>asynchronously
     * closed</i>, or the thread interrupted during the read, is highly input
     * stream specific, and therefore not specified.
     *
     * <p> If an I/O error occurs reading from the input stream, then it may do
     * so after some, but not all, bytes have been read. Consequently the input
     * stream may not be at end of stream and may be in an inconsistent state.
     * It is strongly recommended that the stream be promptly closed if an I/O
     * error occurs.
     *
     * @implNote
     * The number of bytes allocated to read data from this stream and return
     * the result is bounded by {@code 2*(long)len}, inclusive.
     *
     * @param len the maximum number of bytes to read
     * @return a byte array containing the bytes read from this input stream
     * @throws IllegalArgumentException if {@code length} is negative
     * @throws IOException if an I/O error occurs
     * @throws OutOfMemoryError if an array of the required size cannot be
     *         allocated.
     *
     * @since 11
     */
    public byte[] readNBytes(int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("len < 0");
        }

        List<byte[]> bufs = null;
        byte[] result = null;
        int total = 0;
        int remaining = len;
        int n;
        do {
            byte[] buf = new byte[Math.min(remaining, DEFAULT_BUFFER_SIZE)];
            int nread = 0;

            // read to EOF which may read more or less than buffer size
            while ((n = read(buf, nread,
                Math.min(buf.length - nread, remaining))) > 0) {
                nread += n;
                remaining -= n;
            }

            if (nread > 0) {
                if (MAX_BUFFER_SIZE - total < nread) {
                    throw new OutOfMemoryError("Required array size too large");
                }
                total += nread;
                if (result == null) {
                    result = buf;
                } else {
                    if (bufs == null) {
                        bufs = new ArrayList<>();
                        bufs.add(result);
                    }
                    bufs.add(buf);
                }
            }
            // if the last call to read returned -1 or the number of bytes
            // requested have been read then break
        } while (n >= 0 && remaining > 0);

        if (bufs == null) {
            if (result == null) {
                return new byte[0];
            }
            return result.length == total ?
                result : Arrays.copyOf(result, total);
        }

        result = new byte[total];
        int offset = 0;
        remaining = total;
        for (byte[] b : bufs) {
            int count = Math.min(b.length, remaining);
            System.arraycopy(b, 0, result, offset, count);
            offset += count;
            remaining -= count;
        }

        return result;
    }

    /**
     * Reads the requested number of bytes from the input stream into the given
     * byte array. This method blocks until {@code len} bytes of input data have
     * been read, end of stream is detected, or an exception is thrown. The
     * number of bytes actually read, possibly zero, is returned. This method
     * does not close the input stream.
     *
     * <p> In the case where end of stream is reached before {@code len} bytes
     * have been read, then the actual number of bytes read will be returned.
     * When this stream reaches end of stream, further invocations of this
     * method will return zero.
     *
     * <p> If {@code len} is zero, then no bytes are read and {@code 0} is
     * returned; otherwise, there is an attempt to read up to {@code len} bytes.
     *
     * <p> The first byte read is stored into element {@code b[off]}, the next
     * one in to {@code b[off+1]}, and so on. The number of bytes read is, at
     * most, equal to {@code len}. Let <i>k</i> be the number of bytes actually
     * read; these bytes will be stored in elements {@code b[off]} through
     * {@code b[off+}<i>k</i>{@code -1]}, leaving elements {@code b[off+}<i>k</i>
     * {@code ]} through {@code b[off+len-1]} unaffected.
     *
     * <p> The behavior for the case where the input stream is <i>asynchronously
     * closed</i>, or the thread interrupted during the read, is highly input
     * stream specific, and therefore not specified.
     *
     * <p> If an I/O error occurs reading from the input stream, then it may do
     * so after some, but not all, bytes of {@code b} have been updated with
     * data from the input stream. Consequently the input stream and {@code b}
     * may be in an inconsistent state. It is strongly recommended that the
     * stream be promptly closed if an I/O error occurs.
     *
     * @param  b the byte array into which the data is read
     * @param  off the start offset in {@code b} at which the data is written
     * @param  len the maximum number of bytes to read
     * @return the actual number of bytes read into the buffer
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if {@code b} is {@code null}
     * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len}
     *         is negative, or {@code len} is greater than {@code b.length - off}
     *
     * @since 9
     */
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);

        int n = 0;
        while (n < len) {
            int count = read(b, off + n, len - n);
            if (count < 0)
                break;
            n += count;
        }
        return n;
    }

    /**
     * Resets this stream to the last marked location. Throws an
     * {@code IOException} if the number of bytes read since the mark has been
     * set is greater than the limit provided to {@code mark}, or if no mark
     * has been set.
     * <p>
     * This implementation always throws an {@code IOException} and concrete
     * subclasses should provide the proper implementation.
     *
     * @throws IOException
     *             if this stream is closed or another IOException occurs.
     */
    public synchronized void reset() throws IOException {
        throw new IOException();
    }

    /**
     * Skips at most {@code byteCount} bytes in this stream. The number of actual
     * bytes skipped may be anywhere between 0 and {@code byteCount}. If
     * {@code byteCount} is negative, this method does nothing and returns 0, but
     * some subclasses may throw.
     *
     * <p>Note the "at most" in the description of this method: this method may
     * choose to skip fewer bytes than requested. Callers should <i>always</i>
     * check the return value.
     *
     * <p>This default implementation reads bytes into a temporary buffer. Concrete
     * subclasses should provide their own implementation.
     *
     * @return the number of bytes actually skipped.
     * @throws IOException if this stream is closed or another IOException
     *             occurs.
     */
    public long skip(long byteCount) throws IOException {
        return Streams.skipByReading(this, byteCount);
    }

    /**
     * Reads all bytes from this input stream and writes the bytes to the
     * given output stream in the order that they are read. On return, this
     * input stream will be at end of stream. This method does not close either
     * stream.
     * <p>
     * This method may block indefinitely reading from the input stream, or
     * writing to the output stream. The behavior for the case where the input
     * and/or output stream is <i>asynchronously closed</i>, or the thread
     * interrupted during the transfer, is highly input and output stream
     * specific, and therefore not specified.
     * <p>
     * If an I/O error occurs reading from the input stream or writing to the
     * output stream, then it may do so after some bytes have been read or
     * written. Consequently the input stream may not be at end of stream and
     * one, or both, streams may be in an inconsistent state. It is strongly
     * recommended that both streams be promptly closed if an I/O error occurs.
     *
     * @param  out the output stream, non-null
     * @return the number of bytes transferred
     * @throws IOException if an I/O error occurs when reading or writing
     * @throws NullPointerException if {@code out} is {@code null}
     *
     * @since 9
     */
    public long transferTo(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = this.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }
}
