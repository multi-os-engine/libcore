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
public class NativeAllocation {

    /**
     * Registers a native allocation so that the heap knows about it and performs GC as required.
     */
    public static void registerNativeAllocation(long bytes) {
        // TODO: Change the runtime to support passing bytes as a long instead
        // of an int. For now, we clamp bytes to fit.
        bytes = Math.min(bytes, Integer.MAX_VALUE);
        VMRuntime.getRuntime().registerNativeAllocation((int)bytes);
    }

    /**
     * Registers a native free by reducing the number of native bytes accounted for.
     */
    public static void registerNativeFree(long bytes) {
        // TODO: Change the runtime to support passing bytes as a long instead
        // of an int. For now, we clamp bytes to fit.
        bytes = Math.min(bytes, Integer.MAX_VALUE);
        VMRuntime.getRuntime().registerNativeFree((int)bytes);
    }


    /**
     * Registers a native free by associating it with a given referent Java
     * object.
     * This is an alternative to calling registerNativeFree(long) to free bytes
     * registered with registerNativeAllocation. This notifies the runtime
     * that the bytes should automatically be freed when the referent Java
     * object becomes unreachable.
     * <p>
     * The freeFunction should have type: void f(void*); it will automatically
     * be called in a timely fashion when the referent object becomes
     * unreachable. If you maintain references to the underlying native
     * allocation outside of the referent object, you must not access these
     * after the referent becomes unreachable, because they may be dangling
     * pointers.
     * <p>
     * The returned Runnable can be used to free the native allocation before
     * the referent object becomes unreachable.
     *
     * @param referent      java object to associate the native allocation with
     * @param nativePtr     address of the native allocation
     * @param freeFunction  address of a native function to free the native allocation
     * @param size          size the native allocation was registered with
     * @return runnable to explicitly free native allocation
     * @throws IllegalArgumentException If the size is negative or freeFunction is 0
     *
     * @hide
     */
    public static Runnable registerNativeFree(Object referent,
            long nativePtr, long freeFunction, long size) {
        if (size < 0) {
            throw new IllegalArgumentException("Invalid native free size: " + size);
        }

        if (freeFunction == 0) {
            throw new IllegalArgumentException("freeFunction is 0.");
        }
        NativeFreer freer = new NativeFreer(nativePtr, freeFunction, size);
        Cleaner.create(referent, freer);
        return freer;
    }

    /**
     * A Runnable to free a native allocation associated with a referent Java object.
     * The NativeFreer may be used to explicitly free the native allocation.
     * Otherwise, the runtime will automatically free the native allocation
     * when the referent Java object becomes unreachable.
     */
    private static class NativeFreer implements Runnable {
        // These fields are set to 0 to indicate the native allocation has
        // been freed.
        private long nativePtr;
        private long freeFunction;
        private long size;

        public NativeFreer(long nativePtr, long freeFunction, long size) {
            this.nativePtr = nativePtr;
            this.freeFunction = freeFunction;
            this.size = size;
        }

        /**
         * Free the underlying native allocation by calling the freeFunction
         * provided when initially registering the native allocation to be
         * freed. This will notify the runtime to lower the heap pressure
         * accordingly. This has no effect if the native allocation has
         * already been freed.
         * <p>
         * This method is not thread safe.
         */
        public void run() {
            if (freeFunction != 0) {
                nativeFree(freeFunction, nativePtr);
                nativePtr = 0;
                freeFunction = 0;
                size = 0;

                registerNativeFree(size);
            }
        }
    }

    // Calls freeFunction(ptr).
    private static native void nativeFree(long freeFunction, long ptr);
}

