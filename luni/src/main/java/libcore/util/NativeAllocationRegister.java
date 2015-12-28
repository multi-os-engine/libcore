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
 * limitations under the License
 */

package libcore.util;

import dalvik.system.VMRuntime;
import sun.misc.Cleaner;

/**
 * Provides methods to register native allocations with the runtime and
 * associate them with Java objects.
 * @hide
 */
public class NativeAllocationRegister {

    private final long freeFunction;
    private final int size;

    /**
     * Construct a NativeAllocationRegister for native allocations of the
     * given size that should be freed with freeFunction.
     * freeFunction should be the address of a native function to free a
     * native allocation. The type of the native function should be
     * void f(void*);
     *
     * @throws IllegalArgumentException If the size is negative.
     */
    public NativeAllocationRegister(long freeFunction, long size) {
        if (size < 0) {
            throw new IllegalArgumentException("Invalid native free size: " + size);
        }

        this.freeFunction = freeFunction;

        // TODO: Change the runtime to support passing the size as a long
        // instead of an int. For now, we clamp the size to fit.
        this.size = (int)Math.min(size, Integer.MAX_VALUE);
    }

    /**
     * Registers a new native allocation so that the heap knows about it and
     * performs GC as required. This function should ideally be called before
     * performing the underlying native allocation, to ensure there is enough
     * space ahead of time to register the allocation.
     *
     * @throws OutOfMemoryError  if there is not enough space on the Java heap
     *                           in which to register the allocation.
     */
    public void registerNativeAllocation() {
        VMRuntime.getRuntime().registerNativeAllocation(this.size);
    }

    /**
     * Registers a native free by associating it with a given referent Java
     * object.
     * This notifies the runtime that the native allocation should
     * automatically be freed when the referent Java object becomes
     * unreachable.
     * <p>
     * The freeFunction will automatically be called in a timely fashion when
     * the referent object becomes unreachable. If you maintain references to
     * the underlying native allocation outside of the referent object, you
     * must not access these after the referent becomes unreachable, because
     * they may be dangling pointers.
     * <p>
     * The returned Runnable can be used to free the native allocation before
     * the referent object becomes unreachable.
     *
     * @param referent      java object to associate the native allocation with
     * @param nativePtr     address of the native allocation
     * @return runnable to explicitly free native allocation
     */
    public Runnable registerNativeFree(Object referent, long nativePtr) {
        Cleaner cleaner = Cleaner.create(referent, new CleanerThunk(nativePtr));
        return new CleanerRunner(cleaner);
    }

    private class CleanerThunk implements Runnable {
        private final long nativePtr;

        public CleanerThunk(long nativePtr) {
            this.nativePtr = nativePtr;
        }

        public void run() {
            applyFreeFunction(freeFunction, nativePtr);
            VMRuntime.getRuntime().registerNativeFree(size);
        }
    }

    // TODO: Can we make Cleaner an instance of Runnable so we don't need
    // to make these wrapper objects?
    private static class CleanerRunner implements Runnable {
        private final Cleaner cleaner;

        public CleanerRunner(Cleaner cleaner) {
            this.cleaner = cleaner;
        }

        public void run() {
            cleaner.clean();
        }
    }

    // Calls freeFunction(ptr).
    public static native void applyFreeFunction(long freeFunction, long ptr);
}

