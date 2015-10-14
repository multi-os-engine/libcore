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

package java.lang.ref;

import dalvik.system.VMRuntime;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * A reference to a Java object associated with a native allocation.
 *
 * This is used to implement VMRuntime.NativeAllocation for
 * VMRuntime.registerNativeAllocation.
 *
 * NativeReferences are PhantomReferences that have a pointer to a native
 * allocation. In order for a NativeReference to properly free the native
 * allocation, we must ensure the NativeReference is kept reachable until the
 * native allocation is freed. In order to ensure that the NativeReferences
 * are reachable, NativeReference objects form nodes in a global linked list.
 *
 * @hide
 */
public final class NativeReference extends PhantomReference<Object>
                                   implements VMRuntime.NativeAllocation {
    // The ReferenceQueue used for all NativeReference objects.
    private static final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();

    // The head of the global list of NativeReference that must be held live
    // for them to be enqueued. NativeReference's that do not have an
    // underlying native allocation set are not stored on this list.
    private static NativeReference head = null;

    // Lock to guard the global list of NativeReferences.
    private static final Object lock = new Object();

    // Links used to construct the global list of NativeReferences.
    private NativeReference prev;
    private NativeReference next;

    private long nativePtr;
    private long freeFunction;
    private long size;

    /**
     * Create a NativeReference instance tied to the given referent, but not
     * associated with an underlying native allocation.
     */
    public NativeReference(Object referent) {
        super(referent, queue);
        this.nativePtr = 0;
        this.freeFunction = 0;
        this.size = 0;
    }

    @Override
    public void resetNativeAllocation(long nativePtr, long freeFunction, long size) {
        freeNativeAllocation();
        if (freeFunction != 0) {
          this.nativePtr = nativePtr;
          this.freeFunction = freeFunction;
          this.size = size;

          // TODO: registerNativeAllocation should accept a long for the size.
          VMRuntime.getRuntime().registerNativeAllocation((int)size);
          addNativeAllocation(this);
        }
    };

    @Override
    public void freeNativeAllocation() {
        if (freeFunction != 0) {
            nativeFreeNativeAllocation(nativePtr, freeFunction);

            // TODO: registerNativeFree should accept a long for the size.
            VMRuntime.getRuntime().registerNativeFree((int)size);
            removeNativeAllocation(this);

            nativePtr = 0;
            freeFunction = 0;
            size = 0;
        }
    }

    // Adds the given NativeReference to the global list of NativeReferences.
    private static void addNativeAllocation(NativeReference ref) {
        synchronized (lock) {
            ref.prev = null;
            ref.next = head;
            if (head != null) {
                head.prev = ref;
            }
            head = ref;
        }
    }

    // Removes the given NativeReference from the global list of
    // NativeReferences.
    private static void removeNativeAllocation(NativeReference ref) {
        synchronized (lock) {
            NativeReference next = ref.next;
            NativeReference prev = ref.prev;
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

    /**
     * Get the ReferenceQueue for the NativeReferences.
     */
    public static ReferenceQueue<Object> getQueue() {
        return queue;
    }

    // Calls freeFunction(ptr).
    private static native void nativeFreeNativeAllocation(long ptr, long freeFunction);
}
