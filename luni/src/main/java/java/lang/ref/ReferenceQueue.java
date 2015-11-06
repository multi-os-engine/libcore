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

package java.lang.ref;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ReferenceQueue} is the container on which reference objects are
 * enqueued when the garbage collector detects the reachability type specified
 * for the referent.
 *
 * @since 1.2
 */
public class ReferenceQueue<T> {
    private static final int NANOS_PER_MILLI = 1000000;

    private Reference<? extends T> head;
    private Reference<? extends T> tail;

    /**
     * Constructs a new instance of this class.
     */
    public ReferenceQueue() {
    }

    /**
     * Returns the next available reference from the queue, removing it in the
     * process. Does not wait for a reference to become available.
     *
     * @return the next available reference, or {@code null} if no reference is
     *         immediately available
     */
    @SuppressWarnings("unchecked")
    public synchronized Reference<? extends T> poll() {
        if (head == null) {
            return null;
        }

        Reference<? extends T> ret = head;

        if (head == tail) {
            tail = null;
            head = null;
        } else {
            head = head.queueNext;
        }

        ret.queueNext = null;
        return ret;
    }

    /**
     * Returns the next available reference from the queue, removing it in the
     * process. Waits indefinitely for a reference to become available.
     *
     * @throws InterruptedException if the blocking call was interrupted
     */
    public Reference<? extends T> remove() throws InterruptedException {
        return remove(0L);
    }

    /**
     * Returns the next available reference from the queue, removing it in the
     * process. Waits for a reference to become available or the given timeout
     * period to elapse, whichever happens first.
     *
     * @param timeoutMillis maximum time to spend waiting for a reference object
     *     to become available. A value of {@code 0} results in the method
     *     waiting indefinitely.
     * @return the next available reference, or {@code null} if no reference
     *     becomes available within the timeout period
     * @throws IllegalArgumentException if {@code timeoutMillis < 0}.
     * @throws InterruptedException if the blocking call was interrupted
     */
    public synchronized Reference<? extends T> remove(long timeoutMillis)
            throws InterruptedException {
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("timeout < 0: " + timeoutMillis);
        }

        if (head != null) {
            return poll();
        }

        // avoid overflow: if total > 292 years, just wait forever
        if (timeoutMillis == 0 || (timeoutMillis > Long.MAX_VALUE / NANOS_PER_MILLI)) {
            do {
                wait(0);
            } while (head == null);
            return poll();
        }

        // guaranteed to not overflow
        long nanosToWait = timeoutMillis * NANOS_PER_MILLI;
        int timeoutNanos = 0;

        // wait until notified or the timeout has elapsed
        long startTime = System.nanoTime();
        while (true) {
            wait(timeoutMillis, timeoutNanos);
            if (head != null) {
                break;
            }
            long nanosElapsed = System.nanoTime() - startTime;
            long nanosRemaining = nanosToWait - nanosElapsed;
            if (nanosRemaining <= 0) {
                break;
            }
            timeoutMillis = nanosRemaining / NANOS_PER_MILLI;
            timeoutNanos = (int) (nanosRemaining - timeoutMillis * NANOS_PER_MILLI);
        }
        return poll();
    }

    /**
     * Enqueue the reference object on the receiver. The caller is responsible
     * for ensuring the lock is held on this queue, and for calling notify on
     * this queue after the reference has been enqueued.
     *
     * @param reference
     *            reference object to be enqueued.
     */
    void enqueueInternal(Reference<? extends T> reference) {
        if (tail == null) {
            head = reference;
        } else {
            tail.queueNext = reference;
        }

        // The newly enqueued reference becomes the new tail, and always
        // points to itself.
        tail = reference;
        tail.queueNext = reference;
    }

    /**
     * Enqueue the reference object on the receiver.
     *
     * @param reference
     *            reference object to be enqueued.
     */
    synchronized void enqueue(Reference<? extends T> reference) {
        enqueueInternal(reference);
        notify();
    }

    /**
     * Returns the list of all available references on the queue, removing
     * them in the process. Does not wait for a reference to become available.
     *
     * @return a list of available references, or {@code null} if no reference
     *         is immediately available.
     *
     * @hide
     */
    public synchronized List<Reference<? extends T>> pollAll() {
        if (head == null) {
            return null;
        }

        List<Reference<? extends T>> refs = new ArrayList<Reference<? extends T>>();
        while (head != tail) {
            Reference<? extends T> ref = head;
            refs.add(ref);
            head = ref.queueNext;
            ref.queueNext = null;
        }
        refs.add(head);
        head.queueNext = null;
        tail = null;
        head = null;
        return refs;
    }

    /**
     * Enqueue the given list of currently pending (unenqueued) references.
     *
     * @hide
     */
    public static void enqueuePending(Reference<?> list) {
        Reference<?> start = list;
        do {
            ReferenceQueue queue = list.queue;
            if (queue == null) {
                Reference<?> next = list.pendingNext;
                list.pendingNext = null;
                list = next;
            } else {
                synchronized (queue) {
                    do {
                        Reference<?> next = list.pendingNext;
                        list.pendingNext = null;
                        list.enqueueInternal();
                        list = next;
                    } while (list != start && list.queue == queue);
                    queue.notify();
                }
            }
        } while (list != start);
    }

    /** @hide */
    public static Reference<?> unenqueued = null;

    static void add(Reference<?> list) {
        synchronized (ReferenceQueue.class) {
            if (unenqueued == null) {
                unenqueued = list;
            } else {
                // Find the last element in unenqueued.
                Reference<?> last = unenqueued;
                while (last.pendingNext != unenqueued) {
                  last = last.pendingNext;
                }
                // Add our list to the end. Update the pendingNext to point back to enqueued.
                last.pendingNext = list;
                last = list;
                while (last.pendingNext != list) {
                    last = last.pendingNext;
                }
                last.pendingNext = unenqueued;
            }
            ReferenceQueue.class.notifyAll();
        }
    }
}
