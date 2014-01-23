/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An interface for channels that keep a pointer to a current position within a byte-based entity
 * such as a file.
 * <p>
 * SeekableByteChannels have a pointer into the entity which is referred to as a <em>position</em>.
 * The position can be manipulated by moving it within the entity, and the current position can be
 * queried.
 * <p>
 * SeekableByteChannels also have an associated <em>size</em>. The size of the entity is the number
 * of bytes that it currently contains. The size can be manipulated by adding more bytes to the end
 * of the entity or by removing bytes from the end of the entity. See {@link #truncate},
 * {@link #position} and {@link #write} for details. The current size can also be queried.
 *
 * @hide Until ready for a public API change
 * @since 1.7
 */
public interface SeekableByteChannel extends ByteChannel {

  /**
   * Returns the current position as a positive integer number of bytes from the start of the
   * entity.
   *
   * @throws ClosedChannelException if this channel is closed.
   * @throws IOException            if another I/O error occurs.
   */
  long position() throws IOException;

  /**
   * Sets the channel's position to {@code newPosition}.
   * <p>
   * The argument is the number of bytes counted from the start of the entity. The position cannot
   * be set to a value that is negative. The new position can be set beyond the current entity
   * size. If set beyond the current entity size, attempts to read will return end-of-file. Write
   * operations will succeed but they will fill the bytes between the current end of the entity
   * and the new position with the required number of (unspecified) byte values.
   *
   * @return the receiver.
   * @throws IllegalArgumentException if the new position is negative.
   * @throws ClosedChannelException   if this channel is closed.
   * @throws IOException              if another I/O error occurs.
   */
  SeekableByteChannel position(long newPosition) throws IOException;

  /**
   * Returns the size of the entity underlying this channel in bytes.
   *
   * @return the size of the entity in bytes.
   * @throws ClosedChannelException if this channel is closed.
   * @throws IOException            if an I/O error occurs while getting the size of the entity.
   */
  long size() throws IOException;

  /**
   * Truncates the entity underlying this channel to a given size. Any bytes beyond the given size
   * are removed from the entity. If there are no bytes beyond the given size then the entity
   * contents are unmodified.
   * <p>
   * If the position is currently greater than the given size, then it is set to the new size.
   *
   * @param size the maximum size of the underlying entity.
   * @return this channel.
   * @throws IllegalArgumentException    if the requested size is negative.
   * @throws ClosedChannelException      if this channel is closed.
   * @throws NonWritableChannelException if the channel cannot be written to.
   * @throws IOException                 if another I/O error occurs.
   */
  SeekableByteChannel truncate(long size) throws IOException;

  /**
   * Writes bytes from the given byte buffer to this channel.
   * <p>
   * The bytes are written starting at the current position, and after some number of bytes are
   * written (up to the remaining number of bytes in the buffer) the position is increased by the
   * number of bytes actually written.
   * <p>
   * If the position is beyond the current end of the entity, then the entity is first
   * extended up to the given position by the required number of unspecified
   * byte values.
   *
   * @param buffer the byte buffer containing the bytes to be written.
   * @return the number of bytes actually written.
   * @throws NonWritableChannelException if the channel was not opened for writing.
   * @throws ClosedChannelException      if the channel was already closed.
   * @throws AsynchronousCloseException  if another thread closes the channel during the write.
   * @throws ClosedByInterruptException  if another thread interrupts the calling thread while this
   *                                     operation is in progress. The interrupt state of the
   *                                     calling
   *                                     thread is set and the channel is closed.
   * @throws IOException                 if another I/O error occurs, details are in the message.
   */
  @Override
  int write(ByteBuffer buffer) throws IOException;
}
