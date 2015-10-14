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

/**
 * A class associated with a native allocation. Users are encouraged to
 * subclass this class to implement a class that makes use of a native
 * allocation.
 *
 * @hide
 */
public class NativeAllocation {
    // The pointer to the underlying native allocation.
    // TODO: Should this be marked 'transient'?
    protected long nativePtr;

    private long mNativeAllocationFreeFunction;
    private long mNativeAllocationSize;

    /**
     * Create a NativeAllocation instance not associated with an underlying
     * native allocation.
     */
    protected NativeAllocation() {
        nativePtr = 0;
        mNativeAllocationFreeFunction = 0;
        mNativeAllocationSize = 0;
    }

    /**
     * Reset this NativeAllocation to be associated with the given underlying
     * native allocation.
     *
     * If this NativeAllocation object is already associated with an
     * underlying native allocation, that underlying native allocation will be
     * freed first, as if by calling freeNativeAllocation.  This updates the
     * nativePtr field.  The underlying native allocation will automatically
     * be freed when this NativeAllocation object becomes unreachable, using
     * the provided freeFunction. The freeFunction should have the have the
     * following signature: void freeFunction(void* ptr); The size specified
     * should be the logical size of the underlying native allocation. It is
     * used to incur heap pressure and reported in heap dumps.
     */
    protected void resetNativeAllocation(long nativePtr, long freeFunction, long size) {
        freeNativeAllocation();
        this.nativePtr = nativePtr;
        mNativeAllocationFreeFunction = freeFunction;
        mNativeAllocationSize = size;

        // TODO: registerNativeAllocation should accept a long for the size.
        VMRuntime.getRuntime().registerNativeAllocation((int)size);
    };

    /**
     * Free the underlying native allocation by calling the free function
     * provided when the native allocation was created.
     * This will adjust heap pressure appropriately based on the logical size
     * of the native allocation. After calling this, nativePtr will be set to
     * 0.
     */
    protected void freeNativeAllocation() {
        nativeFreeNativeAllocation(nativePtr, mNativeAllocationFreeFunction);

        // TODO: registerNativeFree should accept a long for the size.
        VMRuntime.getRuntime().registerNativeFree((int)mNativeAllocationSize);

        nativePtr = 0;
        mNativeAllocationFreeFunction = 0;
        mNativeAllocationSize = 0;
    }

    /**
     * Return the logical size of the underlying native allocation associated
     * with this NativeAllocation object.
     */
    protected long getNativeAllocationSize() {
        return mNativeAllocationSize;
    }

    @Override protected void finalize() {
        freeNativeAllocation();
    }

    private static native void nativeFreeNativeAllocation(long ptr, long freeFunction);
}
