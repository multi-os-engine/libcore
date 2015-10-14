/*
 * Copyright (C) 2015 The Android Open Source Project
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

package dalvik.system;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * A handle to a NativeAllocation.
 *
 * @hide
 */
public final class NativeAllocation extends PhantomReference<Object> {
    // TODO: Can this be private instead of public?
    public static final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();
    // Guards the list (not the queue).
    private static final Object LIST_LOCK = new Object();

    // This list contains NativeAllocation that must be held live for them to
    // be enqueued. NativeAllocation's that aren't associated with an
    // underlying native allocation are not stored on this list.
    private static NativeAllocation head = null;

    // The links used to construct the list.
    private NativeAllocation prev;
    private NativeAllocation next;

    private long nativePtr;
    private long freeFunction;
    private long size;

    /**
     * Create a NativeAllocation instance tied to the given referent, but not
     * associated with an underlying native allocation.
     */
    public NativeAllocation(Object referent) {
        super(referent, queue);
        this.nativePtr = 0;
        this.freeFunction = 0;
        this.size = 0;
    }

    /**
     * Reset this NativeAllocation to be associated with the given underlying
     * native allocation.
     *
     * If this NativeAllocation object is already associated with an
     * underlying native allocation, that underlying native allocation will be
     * freed first, as if by calling freeNativeAllocation. The underlying
     * native allocation will automatically be freed when this
     * NativeAllocation's referent object becomes unreachable, using the
     * provided freeFunction. The freeFunction should have the following
     * signature: void freeFunction(void* ptr); The size specified should be
     * the logical size of the underlying native allocation. It is used to
     * incur heap pressure and reported in heap dumps.
     *
     * Note: nativePtr may be null. In this case, the freeFunction will be
     * called with null as its argument when this NativeAllocation object is
     * freed.
     */
    public void resetNativeAllocation(long nativePtr, long freeFunction, long size) {
        freeNativeAllocation();
        this.nativePtr = nativePtr;
        this.freeFunction = freeFunction;
        this.size = size;

        // TODO: registerNativeAllocation should accept a long for the size.
        VMRuntime.getRuntime().registerNativeAllocation((int)size);
        addNativeAllocation(this);
    };

    /**
     * Free the underlying native allocation by calling the free function
     * provided when the native allocation was last reset.
     * This will adjust heap pressure appropriately based on the logical size
     * of the native allocation.
     */
    public void freeNativeAllocation() {
        if (freeFunction != 0) {
            nativeFreeNativeAllocation(nativePtr, freeFunction);
        }

        // TODO: registerNativeFree should accept a long for the size.
        VMRuntime.getRuntime().registerNativeFree((int)size);
        removeNativeAllocation(this);

        nativePtr = 0;
        freeFunction = 0;
        size = 0;
    }

    private static void addNativeAllocation(NativeAllocation ref) {
        synchronized (LIST_LOCK) {
            ref.prev = null;
            ref.next = head;
            if (head != null) {
                head.prev = ref;
            }
            head = ref;
        }
    }

    private static void removeNativeAllocation(NativeAllocation ref) {
        synchronized (LIST_LOCK) {
            NativeAllocation next = ref.next;
            NativeAllocation prev = ref.prev;
            ref.next = null;
            ref.prev = null;
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

    // Call freeFunction(ptr).
    private static native void nativeFreeNativeAllocation(long ptr, long freeFunction);
}
