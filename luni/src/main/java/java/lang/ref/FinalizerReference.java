/*
 * Copyright (C) 2011 The Android Open Source Project
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

package java.lang.ref;

/**
 * @hide
 */
public final class FinalizerReference<T> extends Reference<T> {
    // This queue contains those objects eligible for finalization.
    public static final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();

    // This list contains a FinalizerReference for every finalizable object in the heap.
    // Objects in this list may or may not be eligible for finalization yet.
    private static FinalizerReference head = null;

    // The links used to construct the list.
    private FinalizerReference prev;
    private FinalizerReference next;

    private T zombie;

    // Instances of Sentinel cause a FinalizerReference to be added to the list when they're
    // created, by virtue of being finalizable. Because only the runtime itself (not the library)
    // has access to that FinalizerReference, we add a *different* FinalizerReference to the
    // queue straight away. When that FinalizerReference gets processed, we shouldn't try to
    // remove it from the list because it isn't on the list. This field lets us detect that.
    private boolean onList = true;

    public FinalizerReference(T r, ReferenceQueue<? super T> q) {
        super(r, q);
    }

    @Override
    public T get() {
        return zombie;
    }

    @Override
    public void clear() {
        zombie = null;
    }

    static void add(Object referent) {
        FinalizerReference<?> reference = new FinalizerReference<Object>(referent, queue);
        synchronized (FinalizerReference.class) {
            reference.prev = null;
            reference.next = head;
            if (head != null) {
                head.prev = reference;
            }
            head = reference;
        }
    }

    public static void remove(FinalizerReference reference) {
        synchronized (FinalizerReference.class) {
            if (!reference.onList) {
               return;
            }

            FinalizerReference next = reference.next;
            FinalizerReference prev = reference.prev;
            reference.next = null;
            reference.prev = null;
            if (prev != null) {
                prev.next = next;
            } else {
                head = next;
            }
            if (next != null) {
                next.prev = prev;
            }
        }
    }

    /**
     * Returns once all currently-enqueued references have been finalized.
     */
    public static void finalizeAllEnqueued() throws InterruptedException {
        Sentinel sentinel = new Sentinel();
        FinalizerReference<Object> reference = new FinalizerReference<Object>(null, queue);
        reference.zombie = sentinel;
        reference.onList = false;
        reference.enqueueInternal();
        sentinel.awaitFinalization();
    }

    /**
     * A marker object that we can immediately enqueue. When this object's
     * finalize() method is called, we know all previously-enqueued finalizable
     * references have been finalized.
     *
     * <p>Each instance of this class will be finalized twice as it is enqueued
     * directly and by the garbage collector.
     */
    private static class Sentinel {
        boolean finalized = false;
        @Override protected synchronized void finalize() throws Throwable {
            finalized = true;
            notifyAll();
        }
        synchronized void awaitFinalization() throws InterruptedException {
            while (!finalized) {
                wait();
            }
        }
    }
}
